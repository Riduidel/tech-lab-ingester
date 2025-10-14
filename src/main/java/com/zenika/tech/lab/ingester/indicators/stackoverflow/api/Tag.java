package com.zenika.tech.lab.ingester.indicators.stackoverflow.api;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Tag {
	@JsonProperty List<String> synonyms;
	@JsonProperty boolean hasSynonyms;
	@JsonProperty boolean isModeratorOnly;
	@JsonProperty boolean isRequired;
	@JsonProperty int count;
	@JsonProperty String name;
	@JsonProperty("last_activity_date") Date lastActivity;
}
