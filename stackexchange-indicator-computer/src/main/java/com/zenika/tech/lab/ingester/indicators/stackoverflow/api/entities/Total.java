package com.zenika.tech.lab.ingester.indicators.stackoverflow.api.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Total(
	@JsonProperty long total) {}
