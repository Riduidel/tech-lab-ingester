package com.zenika.tech.lab.ingester.indicators.stackoverflow.api.entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StackExchangeList<Type>(
		List<Type> items,
	@JsonProperty("has_more") boolean hasMore,
	@JsonProperty("quota_max") int quotaMax,
	@JsonProperty("quota_remaining") int quotaRemaining)
{}
