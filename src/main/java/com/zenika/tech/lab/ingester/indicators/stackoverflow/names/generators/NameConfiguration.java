package com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators;

import java.util.Collection;
import java.util.Collections;

import org.jilt.Builder;

@Builder
public record NameConfiguration(
		Collection<String> forbiddenSuffix,
		Collection<String> prefixes, 
		Collection<String> suffixes) {
	public NameConfiguration {
		prefixes = prefixes==null ? Collections.emptyList() : prefixes;
		suffixes = suffixes==null ? Collections.emptyList() : suffixes;
		forbiddenSuffix = forbiddenSuffix==null ? Collections.emptyList() : forbiddenSuffix;
	}
}
