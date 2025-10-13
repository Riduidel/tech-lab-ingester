package com.zenika.tech.lab.ingester.indicators.stackoverflow;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.api.StackExchangeClient;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.api.StackExchangeList;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.api.Tag;
import com.zenika.tech.lab.ingester.model.Technology;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TagsService {
	@ConfigProperty(name = "tech-trends.stackexchange.tags.per.page", defaultValue = "100")
	private int tagsPerPage;
	@RestClient StackExchangeClient stackExchange;
	@Inject TagsRepository tagsRepository;

	public boolean hasTagFor(Technology technology) {
		StackExchangeList<Tag> receivedTags = stackExchange.getTags("stackoverflow", 0, tagsPerPage, null, null);
		throw new UnsupportedOperationException("TODO implement TagsService#hasTagFor");
	}
}
