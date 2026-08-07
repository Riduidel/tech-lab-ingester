package com.zenika.tech.lab.ingester.indicators.stackoverflow.names;

import java.util.Collection;

public interface PlatformNameGenerator {
	default String getPlatform() {
		return getClass().getSimpleName();
	}

	/**
	 * Transform technology name into potential valid names.
	 * Rules depend obviously upon the platform ...
	 * @param technologyName
	 * @return
	 */
	Collection<String> transform(String technologyName);
}
