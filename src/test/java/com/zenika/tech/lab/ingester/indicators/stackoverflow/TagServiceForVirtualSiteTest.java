package com.zenika.tech.lab.ingester.indicators.stackoverflow;

import java.util.Optional;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.model.KnownTechnologiesRepository;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.model.KnownTechnology;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.model.Tag;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.model.TagBuilder;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.model.TagRepository;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.NameConfiguration;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.NameConfigurationBuilder;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.SimplePlatformNameGenerator;
import com.zenika.tech.lab.ingester.model.Technology;
import com.zenika.tech.lab.ingester.model.TechnologyBuilder;
import com.zenika.tech.lab.ingester.model.TechnologyRepository;

import io.quarkus.panache.common.Parameters;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Special test allowing us to create one tag, then work on that tag
 */
@QuarkusTest
class TagServiceForVirtualSiteTest {

	private static final String TEST_PLATFORM = "tech_lab";
	private static final Technology TECH_1;
	private static final Technology TECH_2;
	private static final Tag TAG_1_BY_NAME;
	private static final Tag TAG_2_BY_HOMEPAGE_IN_EXCERPT;
	private static final Technology TECH_3;
	private static final Tag TAG_3_BY_REPOSITORY_IN_DESCRIPTION;
	
	@ApplicationScoped
	public static class TechLabPlatformNameGenerator extends SimplePlatformNameGenerator {
		public TechLabPlatformNameGenerator() {
			super(NameConfigurationBuilder.nameConfiguration().build());
		}

		@Override
		public String getPlatform() {
			return TEST_PLATFORM;
		}
	}

	static {
		String TECH_1_NAME = TagServiceForVirtualSiteTest.class.getSimpleName() + "_tech_1";
		TECH_1 = TechnologyBuilder.technology().name(TECH_1_NAME).description("A test technology for valdiating links")
				.homepage(String.format("https://%s.homepage.test", TECH_1_NAME))
				.repositoryUrl(String.format("https://%s.homepage.test", TECH_1_NAME)).platform(TEST_PLATFORM).build();
		TAG_1_BY_NAME = TagBuilder.tag().name(TECH_1_NAME).site(TEST_PLATFORM).build();
		String TECH_2_NAME = TagServiceForVirtualSiteTest.class.getSimpleName() + "_tech_2";
		TECH_2 = TechnologyBuilder.technology().name(TECH_2_NAME).description("A test technology for valdiating links")
				.homepage(String.format("https://%s.homepage.test", TECH_2_NAME))
				.repositoryUrl(String.format("https://%s.homepage.test", TECH_2_NAME)).platform(TEST_PLATFORM).build();
		TAG_2_BY_HOMEPAGE_IN_EXCERPT = TagBuilder.tag()
				// NAME IS DIFFERENT, so it should match on some content
				.name(TECH_2_NAME.replace("_", "-")).site(TEST_PLATFORM).excerpt(TECH_2.homepage).build();
		String TECH_3_NAME = TagServiceForVirtualSiteTest.class.getSimpleName() + "_tech_3";
		TECH_3 = TechnologyBuilder.technology().name(TECH_3_NAME).description("A test technology for valdiating links")
				.homepage(String.format("https://%s.homepage.test", TECH_3_NAME))
				.repositoryUrl(String.format("https://%s.homepage.test", TECH_3_NAME)).platform(TEST_PLATFORM).build();
		TAG_3_BY_REPOSITORY_IN_DESCRIPTION = TagBuilder.tag()
				// NAME IS DIFFERENT, so it should match on some content
				.name(TECH_2_NAME.replace("_", "-")).site(TEST_PLATFORM).wiki(TECH_3.repositoryUrl).build();
	}

	@Inject
	KnownTechnologiesRepository knownTechnologies;
	@Inject
	TechnologyRepository technologies;
	@Inject
	TagRepository tags;
	@Inject
	TagService tested;

	private static Stream<Arguments> can_find_technology_and_link_tag_by_name() {
		return Stream.of(
				Arguments.of("Should find tag by name", TECH_1, TAG_1_BY_NAME));
	}

	@ParameterizedTest(name="{0}")
	@MethodSource
	@TestTransaction
	void can_find_technology_and_link_tag_by_name(
			String description, Technology referenceTech, Tag referenceTag
			) {
		// Given
		technologies.persist(referenceTech);
		tags.persist(referenceTag);
		// When we register tags for the technology
		tested.registerTagsFor(referenceTech);
		// Then we can find back the referenceTag we created for that technology
		Optional<KnownTechnology> maybeItFoundTags = knownTechnologies.findByTechnology(referenceTech);
		Assertions.assertThat(maybeItFoundTags).hasValueSatisfying(foundTags -> {
			Assertions.assertThat(foundTags.linkedTags)
				.containsOnly(findTagByNameAndSite(referenceTag));
		});
	}

	private static Stream<Arguments> cannot_find_technology_and_link_tag_by_other_fields() {
		return Stream.of(
				Arguments.of("Should not find tag by homepage link in excerpt", TECH_2, TAG_2_BY_HOMEPAGE_IN_EXCERPT),
				Arguments.of("Should not find tag by repository link in description", TECH_3, TAG_3_BY_REPOSITORY_IN_DESCRIPTION));
	}

	@ParameterizedTest(name="{0}")
	@MethodSource
	@TestTransaction
	void cannot_find_technology_and_link_tag_by_other_fields(
			String description,Technology referenceTech, Tag referenceTag
			) {
		// Given
		technologies.persist(referenceTech);
		tags.persist(referenceTag);
		// When we register tags for the technology
		Assertions.assertThatThrownBy(() -> {
			tested.registerTagsFor(referenceTech);
		}).isInstanceOf(BadTagMappingsFor.class);
	}

	private Tag findTagByNameAndSite(Tag t) {
		return tags.find("name=:name and site=:site", Parameters.with("name", t.name).and("site", t.site))
				.firstResult();

	}

	private Technology findTechnologyByName(Technology t) {
		return technologies.find("name=:name", Parameters.with("name", t.name)).firstResult();
	}
}
