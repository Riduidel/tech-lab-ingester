package com.zenika.tech.lab.ingester.indicators.stackoverflow;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.zenika.tech.lab.ingester.model.Technology;
import com.zenika.tech.lab.ingester.model.TechnologyRepository;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;

@QuarkusTest
@TestProfile(TagServiceTest.Configuration.class)
class TagServiceTest {

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
	@Inject TagService tested;

	@Test
	void can_find_technology_has_no_tag() {
		// Given a technology with no tag (https://libraries.io/cocoapods/BartyCrouch)
		Technology bartycrouch = technologies.findById(12483L);
		// When
		boolean hasTags = tested.hasTagsFor(bartycrouch);
		// Then
		Assertions.assertThat(hasTags)
			.describedAs("BartyCrouch has no associated tag")
			.isFalse();
	}

}
