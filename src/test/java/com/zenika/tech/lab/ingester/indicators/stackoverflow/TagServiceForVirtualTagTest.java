package com.zenika.tech.lab.ingester.indicators.stackoverflow;

import java.util.Arrays;
import java.util.Map;

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
@Transactional
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
	
	@Inject TechnologyRepository technologies;
	@Inject TagRepository tags;
	@Inject TagService tested;
	
	@BeforeEach
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
	
	@AfterEach void delete_virtual_tag() {
		for(String suffix : Arrays.asList("react", "vue")) {
			Tag specialTag = tags.findBySiteAndName("test", getClass().getSimpleName()+"-"+suffix).get();
			tags.delete(specialTag);
		}
	}

	@Test
	void can_find_technology_and_link_tag() {
		create_virtual_tag();
		try {
			// Given
			Tag specialTag = tags.findBySiteAndName("test", getReactTag()).get();
			Assertions.assertThat(specialTag)
				.extracting(t -> t.technology)
				.isNull();
			Technology react = technologies.findById(2L);
			// When
			tested.registerTagsFor(react);
			// Then
			specialTag = tags.findBySiteAndName("test", getReactTag()).get();
			Assertions.assertThat(specialTag)
				.extracting(t -> t.technology)
				.isNotNull()
				.isEqualTo(react);
			specialTag = tags.findBySiteAndName("test", getVueTag()).get();
			Assertions.assertThat(specialTag)
				.extracting(t -> t.technology)
				.isNull();
		} catch(RuntimeException e) {
			throw e;
		} finally {
			delete_virtual_tag();
		}
	}

}
