package com.zenika.tech.lab.ingester.indicators.stackoverflow.names;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PlatformNameGeneratorsTest {
	
	static Stream<Arguments> can_parse_go_name() {
		return Stream.of(
				Arguments.arguments(new Maven(),"org.junit.jupiter:junit-jupiter", Arrays.asList("junit-jupiter")),
				Arguments.arguments(new Go(),"x/tools", 
						Arrays.asList("tools", "x/tools", "go-x/tools", "go-tools", "tools-go", "x/tools-go"))
				);
	}

	@ParameterizedTest
	@MethodSource
	void can_parse_go_name(PlatformNameGenerator tested, String packageName, List<String> expectedNames) {
		// Given
		// When
		var effectiveNames = tested.transform(packageName);
		// Then
		Assertions.assertThat(effectiveNames).hasSameElementsAs(expectedNames);
	}

}
