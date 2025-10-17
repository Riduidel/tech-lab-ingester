package com.zenika.tech.lab.ingester.indicators.stackoverflow;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.api.entities.TagDefinition;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.api.entities.TagWiki;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

/**
 * The tags repository tries to maintain an updated database of Stackoverflow, 
 * complete with wiki pages (which allow us to link technologies to tags) 
 */
@ApplicationScoped
public class TagRepository implements PanacheRepository<Tag> {
	private final EntityManager entityManager;

	public TagRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Transactional
	public void findOrCreate(String site, TagDefinition tag, Supplier<TagWiki> wikiDownloader) {
		Optional<Tag> existingTag = findBySiteAndName(site, tag.name());
		existingTag.ifPresentOrElse(
				existing -> updateFromTagDefinition(existing, tag, wikiDownloader), 
				() -> this.createFromTag(site, tag, wikiDownloader));
	}

	public Optional<Tag> findBySiteAndName(String site, String name) {
		return this.find("site=:site and name=:name", 
				Parameters
				.with("site", site)
				.and("name", name)).firstResultOptional();
	}

	private Tag createFromTag(String site, TagDefinition tag,
			Supplier<TagWiki> wikiDownloader) {
		TagWiki wiki = wikiDownloader.get();
		Tag t = TagBuilder.tag()
				.site(site)
				.name(tag.name())
				.synonmyms(tag.synonyms()==null ? Arrays.asList() : tag.synonyms())
				.excerpt(wiki.excerpt())
				.wiki(wiki.body())
				.build();
		persist(t);
		return t;
	}

	private Tag updateFromTagDefinition(Tag existing, TagDefinition tag,
			Supplier<TagWiki> wikiDownloader) {
		return existing;
	}

}
