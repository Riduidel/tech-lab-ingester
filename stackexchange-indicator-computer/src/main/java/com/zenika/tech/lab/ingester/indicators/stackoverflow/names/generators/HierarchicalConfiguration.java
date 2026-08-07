package com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators;

import java.util.Collections;
import java.util.List;

import org.jilt.Builder;

@Builder
public record HierarchicalConfiguration (
		NameConfiguration name,
		List<String> separators,
		boolean supportOptionalSeparator
		) {
	public HierarchicalConfiguration {
		name = name==null ? NameConfigurationBuilder.nameConfiguration().build() : name;
		separators = separators==null ? Collections.emptyList() : separators;
	}
}
