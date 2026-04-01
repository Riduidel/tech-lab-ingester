package com.zenika.tech.lab.ingester.indicators.stackoverflow.dump;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.model.KnownTechnologiesRepository;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.model.KnownTechnology;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.model.Tag;
import com.zenika.tech.lab.ingester.model.Indicator;
import com.zenika.tech.lab.ingester.model.Technology;
import com.zenika.tech.lab.ingester.model.TechnologyRepository;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class PostRepositoryTest {

	@Inject PostRepository tested;
	@Inject TechnologyRepository technologies;
	@Inject KnownTechnologiesRepository stackOverflowTechnologies;
	
	@Test
	public void can_count_react_questions_slowly() {
		// Given
		Technology react = technologies.findById(2L);
		Optional<KnownTechnology> knownReactOptional = stackOverflowTechnologies.findByTechnology(react);
		Assertions.assertThat(knownReactOptional).isPresent();
		KnownTechnology knownReact = knownReactOptional.get();
		Optional<Tag> tagOptional = knownReact.linkedTags
			.stream()
			.filter(t -> t.site.equalsIgnoreCase("stackoverflow"))
			.findFirst();
		Assertions.assertThat(tagOptional).isPresent();
		Tag reactTag = tagOptional.get();
		Date start = new Date(2020-1900, 1-1, 1);
		Date end = new Date(2021-1900, 1-1, 1);
		// When
		List<Indicator> indicators = tested.groupQuestionsByMonth(react, reactTag, start, end);
		// Then
		Assertions.assertThat(indicators)
			.hasSize(12)
			;
	}
}
