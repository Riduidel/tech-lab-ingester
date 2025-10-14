package com.zenika.tech.lab.ingester.indicators.stackoverflow.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TagWiki {
@JsonProperty("tag_name") String tagName;
@JsonProperty String excerpt;
@JsonProperty String body;
}
