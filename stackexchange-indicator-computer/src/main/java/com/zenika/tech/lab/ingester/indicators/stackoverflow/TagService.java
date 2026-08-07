package com.zenika.tech.lab.ingester.indicators.stackoverflow;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.api.entities.TagDefinition;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.model.KnownTechnologiesRepository;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.model.KnownTechnology;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.model.KnownTechnologyBuilder;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.model.Tag;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.model.TagRepository;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.NoopPlatformNameTransformer;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.PlatformNameGenerator;
import com.zenika.tech.lab.ingester.model.Technology;

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
	
	Map<String, PlatformNameGenerator> nameTransformers;
	
	@Inject
	public void setNameTransformers(Instance<PlatformNameGenerator> t) {
		nameTransformers = t.stream()
			.collect(Collectors.toMap(
					PlatformNameGenerator::getPlatform, 
					Function.identity()));
	}
	
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
	 * @throws BadTagMappingsFor when invalid mappings are detected
	 */
	@Transactional
	public void registerTagsFor(Technology technology) {
		final KnownTechnology known = knownTechnologies.findByTechnology(technology)
				.orElse(KnownTechnologyBuilder.knownTechnology()
						.technology(technology)
						.known(false)
						.build());
		// If we can find tags, everything is fine
		Set<Tag> tags = getConfiguredTagsFor(technology)
				// Otherwise search for valid tags
				.orElseGet(() -> detectValidTags(known));
		// And persist tags
		persistLinkedTags(known, tags);
	}

	/**
	 * Detect tags on all scanned StackExchange site for given known technology.
	 * For that, we search all corresponding tags with {@link #detectTags(KnownTechnology)}
	 * then check if the collection contains anything.
	 * This is quite contrieved because we may have tags on multiple stackexchange sites.
	 * But things will only work well if we have only one tag per site.
	 * @param known
	 * @return
	 */
	public Set<Tag> detectValidTags(KnownTechnology known) {
		Set<Tag> linkedTags = detectTags(known);
		Map<String, List<Tag>> groupedBySite = groupedBySite(linkedTags);
		// Make sure that we have at most one tag per site
		boolean valid = groupedBySite.values().stream()
				.filter(tagList -> tagList.size()>1)
				.count()==0;
		if(valid) {
			if(linkedTags.isEmpty()) {
				throw new BadTagMappingsFor(known, linkedTags);
			} else {
				return linkedTags;
			}
		} else {
			throw new BadTagMappingsFor(known, linkedTags);
		}
	}

	public Set<Tag> detectTags(KnownTechnology known) {
		Set<Tag> linkedTags = new LinkedHashSet<Tag>();
		List<Function<KnownTechnology, Set<Tag>>> detectorsList = 
				Arrays.asList(
						this::detectPlatformTransformedTagsByName,
						this::detectPlatformTransformedTagsInSynonyms
						);
		for(Function<KnownTechnology, Set<Tag>> detector: detectorsList) {
			linkedTags.addAll(detector.apply(known));
			if(!linkedTags.isEmpty()) {
				break;
			}
		}
		if(linkedTags.isEmpty()) {
//			throw new UnsupportedOperationException("We found to way to map technology to tag ...");
		}
		return linkedTags;
	}

	private Set<Tag> detectPlatformTransformedTagsByName(KnownTechnology known) {
		return getPlatformTransformedTechnologyNames(known)
				.stream()
				.map(name -> new HashSet<Tag>(tags.findByName(name)))
				.filter(set -> !set.isEmpty())
				.findFirst()
				.orElse(new HashSet<Tag>());
	}

	private Set<Tag> detectPlatformTransformedTagsInSynonyms(KnownTechnology known) {
		return getPlatformTransformedTechnologyNames(known)
				.stream()
				.map(name -> new HashSet<Tag>(tags.findByNameInSynonyms(name)))
				.findFirst()
				.orElse(new HashSet<Tag>());
	}

	private Collection<String> getPlatformTransformedTechnologyNames(KnownTechnology known) {
		return nameTransformers.getOrDefault(known.getTechnologyPlatform(), new NoopPlatformNameTransformer(known.getTechnologyPlatform()))
				.transform(known.getTechnologyName());
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
					.map(key -> configuration.getValue(key, String.class))
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

	/**
	 * @param body
	 * @return true if there is a {@link KnownTechnology} object persisted for this technology, even if it has no tags linked
	 */
	@Transactional
	public boolean isKnownTechnology(Technology body) {
		return knownTechnologies.isKnown(body);
	}

	/**
	 * @param technology
	 * @return true if there are stackexchange tags linked to that technology
	 */
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
