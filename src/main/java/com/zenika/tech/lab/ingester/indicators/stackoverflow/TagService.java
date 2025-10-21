package com.zenika.tech.lab.ingester.indicators.stackoverflow;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.api.entities.TagDefinition;
import com.zenika.tech.lab.ingester.model.Technology;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class TagService {
	@Inject TagRepository tags;
	/**
	 * This code fragment is defined in another class to make sure we can test it correctly
	 */
	@Inject TagDefinitionLoader tagLoader;

	/**
	 * @param technology
	 * @return true if there is at least one tag linked to given technology
	 */
	public boolean hasTagsFor(Technology technology) {
		return tags.hasTagsFor(technology);
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
	 */
	@Transactional
	public void registerTagsFor(Technology technology) {
		Set<Tag> linkedTags = new LinkedHashSet<Tag>();
		linkedTags.addAll(tags.findByExcerptContainingUrl(technology.repositoryUrl));
		linkedTags.addAll(tags.findByWikiContainingUrl(technology.repositoryUrl));
		linkedTags.stream()
			.forEach(tag -> {
				tag.technology = technology;
			});
		tags.persist(linkedTags);
	}
}
