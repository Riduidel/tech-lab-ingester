package com.zenika.tech.lab.ingester.indicators.stackoverflow;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.zenika.tech.lab.ingester.model.Technology;
import com.zenika.tech.lab.ingester.model.TechnologyRepository;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Special test allowing us to create one tag, then work on that tag
 */
@QuarkusTest
@TestProfile(TagServiceForVirtualTagTest.Configuration.class)
class TagServiceForVirtualTagTest {

	public static class Configuration implements QuarkusTestProfile {
		@Override
		public Map<String, String> getConfigOverrides() {
			// A stackexchange server with few tags
			return Map.of(
					// See
					// https://github.com/apache/camel-quarkus/issues/6882#issuecomment-2567407689
					"quarkus.camel.routes-discovery.include-patterns", ".*"+TagLoader.class.getSimpleName()
					);
		}
	}
	
	@Inject KnownTechnologiesRepository knownTechnologies;
	@Inject TechnologyRepository technologies;
	@Inject TagRepository tags;
	@Inject TagService tested;
	
	void create_virtual_tag() {
		Technology react = technologies.findById(2L);
		tags.persist(TagBuilder.tag()
				.site("test")
				.name(getReactTag())
				.excerpt(react.repositoryUrl)
				.build());
		tags.persist(TagBuilder.tag()
				.site("test")
				.name(getVueTag())
				.excerpt("")
				.build());
	}

	private String getVueTag() {
		return getClass().getSimpleName()+"-vue";
	}

	private String getReactTag() {
		return getClass().getSimpleName()+"-react";
	}
	
	void delete_virtual_tag() {
		Technology react = technologies.findById(2L);
		Optional<KnownTechnology> knownForReact = knownTechnologies.findByTechnology(react);
		for(String suffix : Arrays.asList("react", "vue")) {
			tags.findBySiteAndName("test", getClass().getSimpleName()+"-"+suffix)
				.ifPresent(special -> {
					knownForReact.ifPresent(k -> {
						k.linkedTags.remove(special);
					});
					tags.delete(special);
				});
		}
		knownForReact.ifPresent(k -> knownTechnologies.persist(k));
	}

	@Test
	@Transactional
	void can_find_technology_and_link_tag() {
		// These were initially annotated BeforeEach/AfterEach
		// But I can't find a way to have these working in conjunction with @Transactional.
		delete_virtual_tag();
		create_virtual_tag();
		try {
			// Given
			Technology react = technologies.findById(2L);
			Assertions.assertThat(react)
				.extracting(r -> r.name)
				.isEqualTo("react");
			// When
			tested.registerTagsFor(react);
			// Then
			Optional<KnownTechnology> reactTags = knownTechnologies.findByTechnology(react);
			Tag testReactTag = tags.findBySiteAndName("test", getReactTag()).get();
			Tag testVueTag = tags.findBySiteAndName("test", getVueTag()).get();
			Assertions.assertThat(reactTags)
				.hasValueSatisfying(k -> {
					Assertions.assertThat(k.linkedTags)
						.contains(testReactTag, testVueTag);
				});
		} catch(RuntimeException e) {
			throw e;
		} finally {
			delete_virtual_tag();
		}
	}

}
