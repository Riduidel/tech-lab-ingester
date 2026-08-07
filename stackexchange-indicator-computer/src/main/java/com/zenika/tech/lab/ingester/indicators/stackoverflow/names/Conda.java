package com.zenika.tech.lab.ingester.indicators.stackoverflow.names;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.NameConfigurationBuilder;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.SimplePlatformNameGenerator;

public class Conda extends SimplePlatformNameGenerator {

	public Conda() {
		super(NameConfigurationBuilder.nameConfiguration()
				.build());
	}

}
