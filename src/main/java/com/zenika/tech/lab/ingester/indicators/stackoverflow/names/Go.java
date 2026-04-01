package com.zenika.tech.lab.ingester.indicators.stackoverflow.names;

import java.util.Arrays;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.HierarchicalConfigurationBuilder;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.HierarchicalPlatformNameGenerator;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.NameConfigurationBuilder;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Go extends HierarchicalPlatformNameGenerator {

	public Go() {
		super(HierarchicalConfigurationBuilder.hierarchicalConfiguration()
				.supportOptionalSeparator(true)
				.separators(Arrays.asList("/"))
				.name(NameConfigurationBuilder.nameConfiguration()
						.prefixes(Arrays.asList("go-"))
						.suffixes(Arrays.asList("-go"))
						.build())
				.build());
	}

}
