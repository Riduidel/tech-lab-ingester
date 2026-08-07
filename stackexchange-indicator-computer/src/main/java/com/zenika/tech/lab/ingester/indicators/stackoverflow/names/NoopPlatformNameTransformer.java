package com.zenika.tech.lab.ingester.indicators.stackoverflow.names;

import java.util.Collection;
import java.util.Collections;

public class NoopPlatformNameTransformer implements PlatformNameGenerator {
	private String platform;

	public NoopPlatformNameTransformer(String platform) {
		this.platform = platform;
	}

	@Override
	public String getPlatform() {
		return platform;
	}

	@Override
	public Collection<String> transform(String technologyName) {
		return Collections.emptyList();
	}

}
