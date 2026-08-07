package com.zenika.tech.lab.ingester.indicators.stackoverflow.names;

import java.util.Arrays;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.NameConfigurationBuilder;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.names.generators.SimplePlatformNameGenerator;

public class Hex extends SimplePlatformNameGenerator{

	public Hex() {
		super(NameConfigurationBuilder.nameConfiguration()
				.build());
	}

}
