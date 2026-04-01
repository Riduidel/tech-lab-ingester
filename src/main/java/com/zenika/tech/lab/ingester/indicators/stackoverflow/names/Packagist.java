package com.zenika.tech.lab.ingester.indicators.stackoverflow.names;

import java.util.Arrays;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.HierarchicalConfigurationBuilder;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.HierarchicalPlatformNameGenerator;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.NameConfigurationBuilder;

public class Packagist extends HierarchicalPlatformNameGenerator{

	public Packagist() {
		super(HierarchicalConfigurationBuilder.hierarchicalConfiguration()
				.supportOptionalSeparator(true)
				.separators(Arrays.asList("/"))
				.name(NameConfigurationBuilder.nameConfiguration()
						.build())
				.build());
	}

}
