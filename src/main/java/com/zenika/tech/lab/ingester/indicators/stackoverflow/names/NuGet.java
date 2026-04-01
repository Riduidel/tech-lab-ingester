package com.zenika.tech.lab.ingester.indicators.stackoverflow.names;

import java.util.Arrays;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.HierarchicalConfigurationBuilder;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.HierarchicalPlatformNameGenerator;

public class NuGet extends HierarchicalPlatformNameGenerator {

	public NuGet() {
		super(HierarchicalConfigurationBuilder.hierarchicalConfiguration()
				.supportOptionalSeparator(false)
				.separators(Arrays.asList("."))
				.build());
	}

}
