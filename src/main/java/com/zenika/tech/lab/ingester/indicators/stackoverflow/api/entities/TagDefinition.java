package com.zenika.tech.lab.ingester.indicators.stackoverflow.api.entities;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TagDefinition(
	@JsonProperty String name,
	@JsonProperty int count,
	@JsonProperty boolean hasSynonyms,
	@JsonProperty List<String> synonyms,
	@JsonProperty boolean isModeratorOnly,
	@JsonProperty boolean isRequired,
	@JsonProperty("last_activity_date") LocalDate lastActivity)
{}
