package com.zenika.tech.lab.ingester.indicators.stackoverflow.names;

import java.util.Arrays;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.NameConfiguration;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.NameConfigurationBuilder;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.SimplePlatformNameGenerator;

public class Hackage extends SimplePlatformNameGenerator {

	public Hackage() {
		super(NameConfigurationBuilder.nameConfiguration()
				.build());
	}

}
