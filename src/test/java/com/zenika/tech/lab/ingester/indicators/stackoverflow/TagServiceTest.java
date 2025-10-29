package com.zenika.tech.lab.ingester.indicators.stackoverflow;

import java.util.Map;
import java.util.Set;

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

	/**
	 * This one ensure we have some way to limit the number of technologies to fix by hand.
	 * Because code is always faster than hand.
	 * 
	 * To find badly mapped technologies, it's easy:
	 * go in DBeaver, and run the following query
	 * 
	 * <pre>
select 
t.id ,
t."name" ,
st."name" ,
st.site 
from stackoverflow_known_technology skt 
join technology t on skt.technology_id =t.id 
join stackoverflow_technology_to_tag sttt on skt.technology_id = sttt.technology_id 
join stackoverflow_tag st on sttt.tag_id = st.id 
where not (UPPER(t."name") like '%'|| UPPER(st."name") ||'%')
order by t.id
;

	 * </pre>
	 * 
	 */
	@Test
	void can_map_mocha_technology_to_mocha_tag() {
		// Given mocha technology
		Technology mocha = technologies.findById(13L);
		Assertions.assertThat(mocha)
			.extracting(t -> t.name)
			.isEqualTo("mocha");
		// We make sure no tag is associated (for now)
		tested.removeTagsFor(mocha);
		// When
		Assertions.assertThat(tested.hasTagsFor(mocha)).isFalse();
		tested.registerTagsFor(mocha);
		Set<Tag> tags = tested.getTagsFor(mocha);
		// Then
		Assertions.assertThat(tags)
			.isNotEmpty()
			.extracting(t -> t.name)
			.contains("mocha.js");
	}

}
