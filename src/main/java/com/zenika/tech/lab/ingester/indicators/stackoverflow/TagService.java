package com.zenika.tech.lab.ingester.indicators.stackoverflow;

import java.util.List;
import java.util.Optional;
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

	public boolean hasTagFor(Technology technology) {
		throw new UnsupportedOperationException("TODO implement TagService#hasTagFor");
	}

	public Long count() {
		return tags.count();
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

	public List<Tag> maybePersist(String site, List<TagDefinition> tagDefinitions) {
		List<String> tagNames = tagDefinitions.stream().map(t->t.name()).collect(Collectors.toList());
		tags.findOrCreate(site, tagDefinitions, () -> tagLoader.loadWikiInfos(site, tagDefinitions));
		// We just created it, so we're quite sure it exists!
		return tags.findBySiteAndNames(site, tagNames)
				.stream()
				.map(Optional::get)
				.collect(Collectors.toList());
	}
}
