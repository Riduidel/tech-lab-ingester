package com.zenika.tech.lab.ingester.indicators.stackoverflow.names;

import java.util.Arrays;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.HierarchicalConfigurationBuilder;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.HierarchicalPlatformNameGenerator;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.NameConfigurationBuilder;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NPM extends HierarchicalPlatformNameGenerator implements PlatformNameGenerator {
	public NPM() {
		super(HierarchicalConfigurationBuilder.hierarchicalConfiguration()
				.supportOptionalSeparator(true)
				.separators(Arrays.asList("/"))
				.name(NameConfigurationBuilder.nameConfiguration()
						.prefixes(Arrays.asList("node.", "js-"))
						.suffixes(Arrays.asList("js", ".js"))
						.build())
				.build());
	}
}
