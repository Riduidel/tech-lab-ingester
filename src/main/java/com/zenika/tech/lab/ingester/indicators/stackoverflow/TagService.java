package com.zenika.tech.lab.ingester.indicators.stackoverflow;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.api.entities.TagDefinition;
import com.zenika.tech.lab.ingester.model.Technology;

import io.quarkus.logging.Log;
import io.smallrye.config.SmallRyeConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class TagService {
	public static final String PREFIX = "tech-lab-ingester.indicators.stackexchange";
	@Inject SmallRyeConfig configuration;
	@Inject TagRepository tags;
	@Inject KnownTechnologiesRepository knownTechnologies;
	/**
	 * This code fragment is defined in another class to make sure we can test it correctly
	 */
	@Inject TagDefinitionLoader tagLoader;

	/**
	 * Check if tag exists locally, and if it is "fresh".
	 * If not existing, or not fresh, we re-download the associated wiki page
	 * @param site site on which tag is declared
	 * @param tag tag to persist
	 */
	@Transactional
	public Tag maybePersist(String site, TagDefinition tag) {
		tags.findOrCreate(site, tag, () -> Optional.of(tagLoader.loadWikiInfos(site, tag)));
		// We just created it, so we're quite sure it exists!
		return tags.findBySiteAndName(site, tag.name()).get();
	}

	public List<Tag> findAll() {
		return tags.findAll().list();
	}

	@Transactional
	public List<Tag> maybePersist(String site, List<TagDefinition> tagDefinitions) {
		List<String> tagNames = tagDefinitions.stream().map(t->t.name()).collect(Collectors.toList());
		tags.findOrCreate(site, tagDefinitions, () -> tagLoader.loadWikiInfos(site, tagDefinitions));
		// We just created it, so we're quite sure it exists!
		return tags.findBySiteAndNames(site, tagNames)
				.stream()
				.map(Optional::get)
				.collect(Collectors.toList());
	}

	/**
	 * Find all tags for given technology across the StackExchange-verse (and associate them to that technology)
	 * @param technology
	 */
	@Transactional
	public void registerTagsFor(Technology technology) {
		KnownTechnology known = knownTechnologies.findByTechnology(technology)
				.orElse(KnownTechnologyBuilder.knownTechnology()
						.technology(technology)
						.known(false)
						.build());
		Optional<List<Tag>> forcedTags = getTags(technology);
		forcedTags.ifPresentOrElse(tags -> registerForcedTagsFor(known, technology, tags), 
				() -> autoRegisterTagsFor(known, technology));
	}

	private void autoRegisterTagsFor(KnownTechnology known, Technology technology) {
		Set<Tag> linkedTags = new LinkedHashSet<Tag>();
		linkedTags.addAll(tags.findByName(technology.name));
		if(linkedTags.isEmpty()) {
			linkedTags.addAll(tags.findByExcerptContainingUrl(technology.repositoryUrl));
			linkedTags.addAll(tags.findByWikiContainingUrl(technology.repositoryUrl));
			Log.infof("Loading tags of %s by repository url added %s", technology, linkedTags);
		}
		if(linkedTags.isEmpty()) {
			linkedTags.addAll(tags.findByExcerptContainingUrl(technology.homepage));
			linkedTags.addAll(tags.findByWikiContainingUrl(technology.homepage));
			Log.infof("Loading tags of %s by homepage added %s", technology, linkedTags);
		}
		Map<String, List<Tag>> groupedBySite = groupedBySite(linkedTags);
		boolean valid = groupedBySite.values().stream()
				.filter(tagList -> tagList.size()>1)
				.count()==0;
		if(valid) {
			persistLinkedTags(known, linkedTags);
		} else {
			String detailedError = groupedBySite.entrySet().stream()
				.filter(entry -> entry.getValue().size()>1)
				.map(entry -> String.format("Site %s has %d tags found: %s", entry.getKey(), entry.getValue().size(), entry.getValue()))
				.collect(Collectors.joining("\n - ", " - ", "\n"));
			Log.warnf("Found tags for technology %s.\n"
					+ "They're not coherent :more than one tag per StackExchange site was detected.\n"
					+ "Invalid sites found are:\n"
					+ "%s"
					+ "You'll have to set that by hand in configuration file", linkedTags, technology, detailedError);
		}
	}

	private Map<String, List<Tag>> groupedBySite(Set<Tag> linkedTags) {
		return linkedTags.stream()
			.collect(Collectors.groupingBy(tag -> tag.site));
	}

	private void persistLinkedTags(KnownTechnology known, Collection<Tag> linkedTags) {
		known.linkedTags.addAll(linkedTags);
		known.known = true;
		knownTechnologies.persist(known);
	}

	private void registerForcedTagsFor(KnownTechnology known, Technology technology, List<Tag> tags) {
		persistLinkedTags(known, tags);
	}

	private Optional<List<Tag>> getTags(Technology technology) {
		String configurationKey = getConfigurationKeyFor(technology);
		if(configuration.isPropertyPresent(configurationKey)) {
			List<String> potential = configuration.getIndexedProperties(configurationKey);
			return Optional.of(potential.stream()
					.map(tagText -> tagText.split(":"))
					.filter(tagArray -> tagArray.length==2)
					.map(tagArray -> 
						tags.findBySiteAndName(tagArray[0], tagArray[1])
					)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.collect(Collectors.toList()));
		}
		return Optional.empty();
	}

	private String getConfigurationKeyFor(Technology technology) {
		return String.format("%s.%s.%s", 
				PREFIX, technology.platform, technology.name);
	}

	@Transactional
	public boolean isKnownTechnology(Technology body) {
		return knownTechnologies.isKnown(body);
	}

	@Transactional
	public boolean hasTagsFor(Technology technology) {
		if(!isKnownTechnology(technology))
			return false;
		Optional<KnownTechnology> known = knownTechnologies.findByTechnology(technology);
		return ! known.map(k -> k.linkedTags)
			.map(Collection::isEmpty)
			.orElse(true);
	}

	/**
	 * Remove all known tags for the given technology.
	 * This helper method should only be used for tests
	 * @param technology
	 */
	@Transactional
	public void removeTagsFor(Technology technology) {
		knownTechnologies.delete(technology);
	}

	public Set<Tag> getTagsFor(Technology technology) {
		return knownTechnologies.findByTechnology(technology)
				.map(k -> k.linkedTags)
				.orElse(Collections.emptySet());
	}
}
