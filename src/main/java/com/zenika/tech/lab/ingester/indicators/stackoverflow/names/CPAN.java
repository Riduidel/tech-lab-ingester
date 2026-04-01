package com.zenika.tech.lab.ingester.indicators.stackoverflow.names;

import java.util.Arrays;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.NameConfiguration;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.NameConfigurationBuilder;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.SimplePlatformNameGenerator;

public class CPAN extends SimplePlatformNameGenerator implements PlatformNameGenerator {

	public CPAN(NameConfiguration configuration) {
		super(NameConfigurationBuilder.nameConfiguration()
				.prefixes(Arrays.asList("r-"))
				.build());
	}
}
