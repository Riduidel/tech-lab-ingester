package com.zenika.tech.lab.ingester.indicators.stackoverflow.dump;

import java.io.IOException;
import java.util.*;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import org.assertj.core.api.Assertions;

import org.junit.jupiter.api.Test;

import com.zenika.tech.lab.ingester.Constants;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.Tags;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.model.KnownTechnologiesRepository;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.model.Tag;
import com.zenika.tech.lab.ingester.model.Indicator;
import com.zenika.tech.lab.ingester.model.Technology;
import com.zenika.tech.lab.ingester.model.TechnologyRepository;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;


@QuarkusTest
@TestProfile(Configuration.class)
@QuarkusTestResource(SqliteDatabaseResource.class)
class PostRepositoryTest implements QuarkusTestProfile {


    @Inject PostRepository tested;
    @Inject TechnologyRepository technologies;
    @Inject KnownTechnologiesRepository stackOverflowTechnologies;


	@Test
	public void can_count_react_questions_slowly() throws IOException {
		// Given
        Technology react = Constants.Technologies.react;
		Tag reactTag = Tags.StackOverflow.react;
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
