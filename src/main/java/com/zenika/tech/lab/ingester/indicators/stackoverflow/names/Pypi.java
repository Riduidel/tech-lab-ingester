package com.zenika.tech.lab.ingester.indicators.stackoverflow.names;

import java.util.Arrays;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.NameConfiguration;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.NameConfigurationBuilder;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.SimplePlatformNameGenerator;

public class Pypi extends SimplePlatformNameGenerator implements PlatformNameGenerator {

	public Pypi(NameConfiguration configuration) {
		super(NameConfigurationBuilder.nameConfiguration()
				.prefixes(Arrays.asList("py", "python-"))
				.suffixes(Arrays.asList("py", ".py"))
				.build());
	}
}
