package com.zenika.tech.lab.ingester.indicators;

import com.zenika.tech.lab.ingester.model.Technology;
import org.apache.camel.Exchange;

public interface TechnologyBased {
	default Technology getTechnology(Exchange exchange) {
		return exchange.getMessage().getBody(Technology.class);
	}
}
