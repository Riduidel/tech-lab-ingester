package com.zenika.tech.lab.ingester.indicators.stackoverflow.names;

import java.util.Arrays;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.HierarchicalConfigurationBuilder;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.HierarchicalPlatformNameGenerator;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Clojars extends HierarchicalPlatformNameGenerator implements PlatformNameGenerator {

	public Clojars() {
		super(HierarchicalConfigurationBuilder.hierarchicalConfiguration()
				.separators(Arrays.asList("/"))
				.supportOptionalSeparator(true)
				.build());
	}
}
