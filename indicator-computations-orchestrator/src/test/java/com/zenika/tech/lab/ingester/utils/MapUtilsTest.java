package com.zenika.tech.lab.ingester.utils;

import java.util.Map;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MapUtilsTest {
	
	private static Stream<Arguments> can_merge_two_maps() {
		return Stream.of(
				Arguments.of(Map.of(), Map.of(), Map.of()),
				Arguments.of(Map.of("a", "b"), Map.of(), Map.of("a", "b")),
				Arguments.of(Map.of(), Map.of("a", "b"), Map.of("a", "b")),
				Arguments.of(
						Map.of("a", "b"), 
						Map.of("c", "d"), 
						Map.of("a", "b", "c", "d")),
				Arguments.of(
						Map.of("a", "b", "c", "d"), 
						Map.of("c", "e"), 
						Map.of("a", "b", "c", "d")),
				Arguments.of(
						Map.of("a", Map.of("b", "c")), 
						Map.of("d", "e"), 
						Map.of("a", Map.of("b", "c"), "d", "e")),
				Arguments.of(
						Map.of("a", Map.of("b", "c")), 
						Map.of("a", Map.of("d", "e")), 
						Map.of("a", Map.of("b", "c", "d", "e")))
				);
	}

	@ParameterizedTest
	@MethodSource
	void can_merge_two_maps(Map<String, Object> first, Map<String, Object> second, Map<String, Object> expected) {
		// Given
		// When
		Map<String, Object> real = MapUtils.deepMerge(first, second);
		// Then
		Assertions.assertThat(real)
			.isEqualTo(expected);
	}

}
