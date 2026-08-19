package com.zenika.tech.lab.ingester.indicators.stackoverflow;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.zenika.tech.lab.ingester.Constants;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.Tags.StackOverflow;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.api.entities.TagDefinition;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.model.KnownTechnology;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.model.KnownTechnologyBuilder;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.model.Tag;
import com.zenika.tech.lab.ingester.model.Technology;
import com.zenika.tech.lab.ingester.model.TechnologyRepository;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

/**
	 * To find badly mapped technologies, it's easy:
	 * go in DBeaver, and run the following query (it will show technologies mapped to tags with different names)
	 * 
	 * <pre>
select 
t.id as technology_id,
t."name"  as technology_name,
st."name" as tag_name,
st.site as tag_site,
st.id as tag_id
from stackoverflow_known_technology skt 
join technology t on skt.technology_id =t.id 
join stackoverflow_technology_to_tag sttt on skt.technology_id = sttt.technology_id 
join stackoverflow_tag st on sttt.tag_id = st.id 
where not (UPPER(t."name") like '%'|| UPPER(st."name") ||'%')
order by t.id
;
	 * </pre>
	 * 
	 * Or (to get technologies with no mapped tag)
	 * <pre>
select 
t."name"  as technology_name
from stackoverflow_known_technology skt 
join technology t on skt.technology_id =t.id 
left join stackoverflow_technology_to_tag sttt on sttt.technology_id = skt.technology_id 
where sttt.technology_id is null
;
	 * </pre>

 */
@QuarkusTest
class TagServiceDetectTagsTest {

	@Inject TechnologyRepository technologies;
	
	@Inject TagService tested;
	
	public static Stream<Arguments> can_detect_tags_for_technology() {
		return Stream.of(
				Arguments.of(
					Constants.Technologies.react,
					Arrays.asList(StackOverflow.react)),
				Arguments.of(
						Constants.Technologies.vue,
						Arrays.asList(StackOverflow.vue)),
				Arguments.of(
						Constants.Technologies.angular,
						// No tag should be detected for angular, because they use a weird dependency naming scheme (or rather microdependencies)
						Arrays.asList()),
				Arguments.of(
					Constants.Technologies.next,
					Arrays.asList(StackOverflow.nextjs)),
				Arguments.of(
						Constants.Technologies.enzyme,
						Arrays.asList(StackOverflow.enzyme)),
				Arguments.of(
						Constants.Technologies.flambo,
						Arrays.asList(StackOverflow.flambo))
				// Can't work, because at this level we don't handle the configured tags!
				// And there *is* a matching clipboard tag 
//				Arguments.of(
//						Constants.Technologies.clipboard,
//						Arrays.asList(StackOverflow.clipboardjs))
			);
	}

	/**
	 * This one ensure we have some way to limit the number of technologies to fix by hand.
	 * Because code is always faster than hand.
	 * 
	 */
	@ParameterizedTest
	@MethodSource
	@TestTransaction
	void can_detect_tags_for_technology(Technology source, List<Tag> expected) {
		// Given
		// We may have case where nothing exists in DB (typically CI)
		// In such a case, we have to register both technology and tags
        source.id = null;
		source = technologies.findOrCreate(source);
		expected = expected.stream()
			.map(t -> new TagDefinition(t.name, 1, false, Collections.emptyList(), false, false, LocalDate.now()))
			.map(t -> tested.maybePersist("stackoverflow", t))
			.collect(Collectors.toList());
		KnownTechnology known = KnownTechnologyBuilder.knownTechnology()
				.known(false)
				.technology(source)
				.build();
		// When
		Set<Tag> found = tested.detectTags(known);
		// Then
		Assertions.assertThat(found)
		// We don't want to test id
			.usingElementComparatorOnFields("site", "name")
			.containsAll(expected);
	}
	
	public static Stream<Arguments> can_register_tags_for_technology() {
		return Stream.of(
				Arguments.of(
					Constants.Technologies.react,
					Arrays.asList(StackOverflow.react)),
				Arguments.of(
						Constants.Technologies.vue,
						Arrays.asList(StackOverflow.vue)),
				Arguments.of(
						Constants.Technologies.angular,
						// No tag should be detected for angular, because they use a weird dependency naming scheme (or rather microdependencies)
						Arrays.asList(StackOverflow.angular))
			);
	}

	@ParameterizedTest
	@MethodSource
	@TestTransaction
	void can_register_tags_for_technology(Technology source, List<Tag> expected) {
		// Given
		// We may have case where nothing exists in DB (typically CI)
		// In such a case, we have to register both technology and tags
        source.id = null;
		source = technologies.findOrCreate(source);
		expected = expected.stream()
			.map(t -> new TagDefinition(t.name, 1, false, Collections.emptyList(), false, false, LocalDate.now()))
			.map(t -> tested.maybePersist("stackoverflow", t))
			.collect(Collectors.toList());
		// When
		tested.registerTagsFor(source);
		// Then
		Assertions.assertThat(tested.isKnownTechnology(source))
			.describedAs("Technology is known")
			.isTrue();
		Assertions.assertThat(tested.hasTagsFor(source))
			.describedAs("There are tags linked to that technology")
			.isTrue();
		Set<Tag> found = tested.getTagsFor(source);
		Assertions.assertThat(found)
			.usingElementComparatorOnFields("site", "name")
			.containsAll(expected);
	}

}
