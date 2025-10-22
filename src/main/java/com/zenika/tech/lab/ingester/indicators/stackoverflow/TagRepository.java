package com.zenika.tech.lab.ingester.indicators.stackoverflow;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.api.entities.TagDefinition;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.api.entities.TagWiki;
import com.zenika.tech.lab.ingester.model.Technology;

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
	public void findOrCreate(String site, TagDefinition tag, Supplier<Optional<TagWiki>> wikiDownloader) {
		Optional<Tag> existingTag = findBySiteAndName(site, tag.name());
		existingTag.ifPresentOrElse(
				existing -> updateFromTagDefinition(existing, tag, wikiDownloader), 
				() -> this.createFromTag(site, tag, wikiDownloader));
	}

	@Transactional
	public void findOrCreate(String site, List<TagDefinition> tagDefinitions, Supplier<List<TagWiki>> multiWikiDownloader) {
		List<String> tagNames = tagDefinitions.stream().map(t->t.name()).collect(Collectors.toList());
		List<Optional<Tag>> existingTags = findBySiteAndNames(site, tagNames);
		boolean areAllDownloaded = existingTags.stream().allMatch(Optional::isPresent);
		if(!areAllDownloaded) {
			List<TagWiki> wikis = multiWikiDownloader.get();
			// Since we have to browse a bunch of lists together, let's do it the old style
			for (int i = 0; i < tagDefinitions.size(); i++) {
				TagDefinition tagDefinition = tagDefinitions.get(i);
				Optional<Tag> present = existingTags.get(i);
				Supplier<Optional<TagWiki>> wikiDownloader = () -> { 
					return wikis
						.stream()
						.filter(tw -> tw.tagName().equals(tagDefinition.name()))
						.findAny()
						;
				};
				present.ifPresentOrElse(
						existing -> updateFromTagDefinition(existing, tagDefinition, wikiDownloader), 
						() -> this.createFromTag(site, tagDefinition, wikiDownloader));
				
			}
		}
	}

	public List<Optional<Tag>> findBySiteAndNames(String site, List<String> tagNames) {
		return tagNames
				.stream()
				.map(name -> findBySiteAndName(site, name))
				.collect(Collectors.toList());
	}

	public Optional<Tag> findBySiteAndName(String site, String name) {
		return this.find("site=:site and name=:name", 
				Parameters
				.with("site", site)
				.and("name", name)).firstResultOptional();
	}

	private Tag createFromTag(String site, TagDefinition tag,
			Supplier<Optional<TagWiki>> wikiDownloader) {
		Optional<TagWiki> wiki = wikiDownloader.get();
		Tag t = TagBuilder.tag()
				.site(site)
				.name(tag.name())
				.synonmyms(tag.synonyms()==null ? Arrays.asList() : tag.synonyms())
				.excerpt(wiki.map(TagWiki::excerpt).orElse(null))
				.wiki(wiki.map(TagWiki::body).orElse(null))
				.build();
		persist(t);
		return t;
	}

	private Tag updateFromTagDefinition(Tag existing, TagDefinition tag,
			Supplier<Optional<TagWiki>> wikiDownloader) {
		return existing;
	}

	public Collection<Tag> findByExcerptContainingUrl(String url) {
		if(url==null || url.isBlank())
			return Collections.emptySet();
		return find("excerpt like :text", 
				Parameters.with("text", "%"+url+"%"))
				.list();
	}

	public Collection<Tag> findByWikiContainingUrl(String url) {
		if(url==null || url.isBlank())
			return Collections.emptySet();
		return find("wiki like :text", 
				Parameters.with("text", "%"+url+"%"))
				.list();
	}

	public Collection<? extends Tag> findByName(String name) {
		if(name==null || name.isBlank())
			return Collections.emptySet();
		return find("name=:text", 
				Parameters.with("text", name))
				.list();
	}

}
