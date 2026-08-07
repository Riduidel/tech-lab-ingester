package com.zenika.tech.lab.ingester.indicators.stackoverflow.api.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TagWiki(
@JsonProperty("tag_name") String tagName,
@JsonProperty String excerpt,
@JsonProperty String body)
{}
