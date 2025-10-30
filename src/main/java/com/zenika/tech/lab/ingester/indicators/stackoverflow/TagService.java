package com.zenika.tech.lab.ingester.indicators.stackoverflow;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.api.entities.TagDefinition;
import com.zenika.tech.lab.ingester.model.Technology;

import io.quarkus.logging.Log;
import io.smallrye.config.SmallRyeConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
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
	 * @throws InvalidTagMappingFor when invalid mappings are detected
	 */
	@Transactional
	public void registerTagsFor(Technology technology) {
		final KnownTechnology known = knownTechnologies.findByTechnology(technology)
				.orElse(KnownTechnologyBuilder.knownTechnology()
						.technology(technology)
						.known(false)
						.build());
		Set<Tag> tags = getConfiguredTagsFor(technology)
				.orElseGet(() -> detectTags(known));
		persistLinkedTags(known, tags);
	}

	private Set<Tag> detectTags(KnownTechnology known) {
		Set<Tag> linkedTags = new LinkedHashSet<Tag>();
		linkedTags.addAll(tags.findByName(known.id.technology.name));
		if(linkedTags.isEmpty()) {
			linkedTags.addAll(tags.findByExcerptContainingUrl(known.id.technology.repositoryUrl));
			linkedTags.addAll(tags.findByWikiContainingUrl(known.id.technology.repositoryUrl));
			Log.infof("Loading tags of %s by repository url added %s", known.id.technology, linkedTags);
		}
		if(linkedTags.isEmpty()) {
			linkedTags.addAll(tags.findByExcerptContainingUrl(known.id.technology.homepage));
			linkedTags.addAll(tags.findByWikiContainingUrl(known.id.technology.homepage));
			Log.infof("Loading tags of %s by homepage added %s", known.id.technology, linkedTags);
		}
		Map<String, List<Tag>> groupedBySite = groupedBySite(linkedTags);
		boolean valid = groupedBySite.values().stream()
				.filter(tagList -> tagList.size()>1)
				.count()==0;
		if(valid) {
			return linkedTags;
		} else {
			throw new InvalidTagMappingFor(known, linkedTags);
		}
	}

	public static Map<String, List<Tag>> groupedBySite(Set<Tag> linkedTags) {
		return linkedTags.stream()
			.collect(Collectors.groupingBy(tag -> tag.site));
	}

	private void persistLinkedTags(KnownTechnology known, Collection<Tag> linkedTags) {
		known.linkedTags.addAll(linkedTags);
		known.known = true;
		knownTechnologies.persist(known);
	}

	private Optional<Set<Tag>> getConfiguredTagsFor(Technology technology) {
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
					.collect(Collectors.toSet()));
		}
		return Optional.empty();
	}

	public static String getConfigurationKeyFor(Technology technology) {
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
