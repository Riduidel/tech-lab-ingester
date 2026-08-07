package com.zenika.tech.lab.ingester.indicators.stackoverflow;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.processor.aggregate.AbstractListAggregationStrategy;
import org.apache.commons.collections4.ListUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.api.entities.TagDefinition;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.model.Tag;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Load Stackoverflow tags and stores them in database
 * (or any StackExchange site provided in the {@link #sites} configuration property)
 */
@ApplicationScoped
public class TagLoader extends EndpointRouteBuilder {
	private static final String STACKEXCHANGE_SITE = "stackexchange.site";

	private static final String STACKEXCHANGE_TAGS_COUNT = STACKEXCHANGE_SITE+".size";

	@ConfigProperty(name="tech-lab-ingester.indicators.stackexchange.tags.sites", defaultValue="stackoverflow")
	List<String> sites;
	@ConfigProperty(name = "tech-lab-ingester.indicators.stackexchange.wikis.per.page", defaultValue = "20")
	int wikisPerPage;
	
	@Inject TagDefinitionLoader loader;
	@Inject TagService tags;

	@Override
	public void configure() throws Exception {
		from(seda(TagLoader.class.getSimpleName()))
			.id(getClass().getSimpleName()+"-1-download-all-tags")
			.log(LoggingLevel.INFO, "Downloading all tags")
			.process(this::selectSites)
			.split(body())
				.setHeader(STACKEXCHANGE_SITE, body())
				.log(LoggingLevel.INFO, "📥 Downloading all tags of ${in.headers.stackexchange.site}")
				.process(this::downloadTagsOf)
				.log(LoggingLevel.INFO, "✅ Downloaded all tags of ${in.headers.stackexchange.site}")
				.log(LoggingLevel.INFO, "🖴 Persisting all tags of ${in.headers.stackexchange.site}")
				.split(body())
					// Do not activate parallel processing yet
					.process(this::persistToTags)
					.end()
				.log(LoggingLevel.INFO, "✅ Persisted all tags of ${in.headers.stackexchange.site}")
				.end()
			// Pretty sure it can be optimized with aggregates
			.process(this::getAllTags)
    		.end();
	}

	public void downloadTagsOf(Exchange e) {
		String site = e.getMessage().getBody(String.class);
		List<TagDefinition> tags = loader.loadTagDefinitionsFrom(site);
		// For ease of descendant process, we split the list
		// into lists of 20 elements here
		e.getMessage().setBody(ListUtils.partition(tags, wikisPerPage));
		// We also set a header to make sure we can later aggregate correctly
		e.getMessage().setHeader(STACKEXCHANGE_TAGS_COUNT, tags.size());
	}

	private void persistToTags(Exchange e) {
		String site = e.getMessage().getHeader(STACKEXCHANGE_SITE, String.class);
		List<TagDefinition> definitions = e.getMessage().getBody(List.class);
		List<Tag> tag = tags.maybePersist(site, definitions);
		e.getMessage().setBody(tag);
	}

	private void getAllTags(Exchange exchange) {
		exchange.getMessage().setBody(tags.findAll());
	}

	private void selectSites(Exchange exchange) {
		exchange.getMessage().setBody(sites);
	}
}
