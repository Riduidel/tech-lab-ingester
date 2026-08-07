package com.zenika.tech.lab.ingester.indicators.stackoverflow.names;

import java.util.Arrays;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.HierarchicalConfiguration;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.HierarchicalConfigurationBuilder;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.HierarchicalPlatformNameGenerator;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.NameConfigurationBuilder;

public class Meteor extends HierarchicalPlatformNameGenerator {

	public Meteor() {
		super(HierarchicalConfigurationBuilder.hierarchicalConfiguration()
				.supportOptionalSeparator(true)
				.separators(Arrays.asList(":"))
				.build());
	}

}
