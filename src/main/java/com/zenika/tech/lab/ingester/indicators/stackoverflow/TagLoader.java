package com.zenika.tech.lab.ingester.indicators.stackoverflow;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.processor.aggregate.AbstractListAggregationStrategy;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.api.entities.TagDefinition;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Load Stackoverflow tags and stores them in database
 */
@ApplicationScoped
public class TagLoader extends EndpointRouteBuilder {
	private static final String STACKEXCHANGE_SITE = "stackexchange.site";

	private static final String STACKEXCHANGE_TAGS_COUNT = STACKEXCHANGE_SITE+".size";

	public class ListOfTagsAggregationStrategy extends AbstractListAggregationStrategy<Tag> {

	    @Override
	    public Tag getValue(Exchange exchange) {
	        // the message body contains a number, so return that as-is
	        return exchange.getIn().getBody(Tag.class);
	    }
	}
	
	@ConfigProperty(name="tech-lab-ingester.indicators.stackexchange.tags.sites", defaultValue="stackoverflow")
	List<String> sites;
	
	@Inject TagDefinitionLoader loader;
	@Inject TagService tags;

	@Override
	public void configure() throws Exception {
		from(direct(getClass().getSimpleName()))
			.id(getClass().getSimpleName()+"-1-download-all-tags")
			.log(LoggingLevel.INFO, "Downloading all tags")
			.setBody(constant(sites))
			.split(body())
				.setHeader(STACKEXCHANGE_SITE, body())
				.log(LoggingLevel.INFO, "📥 Downloading all tags of ${in.headers.stackexchange.site}")
				.process(this::downloadTagsOf)
				.log(LoggingLevel.INFO, "✅ Downloaded all tags of ${in.headers.stackexchange.site}")
				.log(LoggingLevel.INFO, "🖴 Persisting all tags of ${in.headers.stackexchange.site}")
				.split(body())
					.parallelProcessing()
					.process(this::persistToTag)
					.end()
				.log(LoggingLevel.INFO, "✅ Persisted all tags of ${in.headers.stackexchange.site}")
			// Pretty sure it can be optimized with aggregates
			.process(this::getAllTags)
    		.end();
	}

	public void downloadTagsOf(Exchange e) {
		String site = e.getMessage().getBody(String.class);
		List<TagDefinition> tags = loader.loadTagDefinitionsFrom(site);
		e.getMessage().setBody(tags);
		// We also set a header to make sure we can later aggregate correctly
		e.getMessage().setHeader(STACKEXCHANGE_TAGS_COUNT, tags.size());
	}

	private void persistToTag(Exchange e) {
		String site = e.getMessage().getHeader(STACKEXCHANGE_SITE, String.class);
		TagDefinition definition = e.getMessage().getBody(TagDefinition.class);
		Tag tag = tags.maybePersist(site, definition);
		e.getMessage().setBody(tag);
	}

	private void getAllTags(Exchange exchange) {
		exchange.getMessage().setBody(tags.findAll());
	}
}
