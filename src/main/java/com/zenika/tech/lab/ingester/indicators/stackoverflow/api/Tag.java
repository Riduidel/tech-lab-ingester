package com.zenika.tech.lab.ingester.indicators.stackoverflow.api;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Tag {
	@JsonProperty int count;
	@JsonProperty String name;
	@JsonProperty("last_activity_date") Date lastActivity;
}
