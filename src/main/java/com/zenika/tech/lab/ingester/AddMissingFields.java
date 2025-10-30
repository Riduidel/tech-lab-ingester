package com.zenika.tech.lab.ingester;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.EndpointConsumerBuilder;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.model.dataformat.YAMLLibrary;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.InvalidTagMappingFor;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.TagService;
import com.zenika.tech.lab.ingester.librariesio.LibrariesIOClient;
import com.zenika.tech.lab.ingester.librariesio.model.Platform;
import com.zenika.tech.lab.ingester.model.Technology;
import com.zenika.tech.lab.ingester.processors.TechnologyRepositoryProcessor;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import ru.vyarus.yaml.updater.YamlUpdater;

@ApplicationScoped
public class AddMissingFields extends EndpointRouteBuilder {
	private static final String ADD_MISSING_FIELDS = "direct:add-missing-technologies-fields";

	private static final String ADD_MISSING_STACKEXCHANGE_TAG_FAILED = ADD_MISSING_FIELDS+"-stackexchanged-failed";

	@ConfigProperty(name="rejected-platforms", defaultValue="Bower,Carthage,Alcatraz,SwiftPM,Nimble,PureScript")
	private List<String> rejectedPlatforms;

	/**
	 * If this file is not null, it means we're running in dev mode.
	 * In such a case, we can try to update
	 */
	@ConfigProperty(name = TagService.PREFIX+".mapping.folder", defaultValue = "src/main/resources")
	Path configurationFolder;
	@ConfigProperty(name = TagService.PREFIX+".mapping.temp", defaultValue = "stackexchange.yaml.tmp")
	String temporaryConfigurationFile;
	@ConfigProperty(name = TagService.PREFIX+".mapping.file", defaultValue = "stackexchange.yaml")
	String configurationFile;
	@ConfigProperty(name = TagService.PREFIX+".override.mappings", defaultValue = "false")
	boolean overrideMappings;
	
	@RestClient LibrariesIOClient librariesIo;

	TechnologyRepositoryProcessor technologies;

	@Inject
	public void setTechnologies(TechnologyRepositoryProcessor technologies) {
		this.technologies = technologies;
	}
	
	@Inject TagService tags;

	private Map<String, String> platformMappings;
	
    @Override
    public void configure() throws Exception {
    	onException(InvalidTagMappingFor.class)
	    	.handled(true)
	    	.to(ADD_MISSING_STACKEXCHANGE_TAG_FAILED);
    	from(generateStarterEndpoint())
    		.routeId(getClass().getSimpleName()+"-1-get-all-technologies")
    		.description("Get all technologies")
			.log("🔍 Searching for technologies")
			// Load all technologies
			// I think it will be necessary to have some kind of batch processing
			.process(technologies::findAllTechnologies)
			.log("⏳️ Adding missing elements to ${body.size} technologies")
			.split(body())
//				.parallelProcessing()
				.to(ADD_MISSING_FIELDS)
				.end()
			.log("✅ All missing elements have been added")
	    	;
    	from(ADD_MISSING_FIELDS)
			.routeId(getClass().getSimpleName()+"-2-add-missing-fields")
			.description("Add missing fields to technology")
			.process(this::addPlatform)
			.process(this::addStackExchangeTags)
			    		;
    	
    	from(ADD_MISSING_STACKEXCHANGE_TAG_FAILED)
    		.choice()
    			.when(this::shouldOverrideConfiguration)
    			// Marshal exception to content usable by SnakeYAML
    			// Then marshal to configuration file if possible
    				.process(this::convertStackExchangeFailureToInterestingObject)
    				.marshal().yaml(YAMLLibrary.SnakeYAML)
    				// in fact, this file only contains the new content
    				.to(file(configurationFolder.toFile().getPath())
						.charset("utf-8")
						.fileName(temporaryConfigurationFile))
    				// Now we have to merge
    				.process(this::mergeConfiguration)
    			.otherwise()
		    		.process(this::convertStackExchangeFailureToLog)
		    		.log(LoggingLevel.WARN, "${body}")
		    .end()
    		;
    }
	
	private void convertStackExchangeFailureToInterestingObject(Exchange exchange) {
	    InvalidTagMappingFor caused = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, InvalidTagMappingFor.class);
	    exchange.getMessage().setBody(caused.toYaml());
	}

	private EndpointConsumerBuilder generateStarterEndpoint() {
		return direct(getClass().getSimpleName());
	}

	private void addPlatform(Exchange exchange) {
		Technology body = exchange.getMessage().getBody(Technology.class);
		if(body.platform==null) {
			Log.infof("🚚 Adding missing platform to %s", body);
			Technology technology = addPlatform(body);
			exchange.getMessage().setBody(technology);
		}
	}
	
	private void convertStackExchangeFailureToLog(Exchange exchange) {
	    InvalidTagMappingFor caused = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, InvalidTagMappingFor.class);
	    exchange.getMessage().setBody(caused.toLog());
	}
	
	/**
	 * @param exchange
	 * @throws InvalidTagMappingFor
	 */
	private void addStackExchangeTags(Exchange exchange) {
		Technology body = exchange.getMessage().getBody(Technology.class);
		if(!tags.isKnownTechnology(body)) {
			tags.registerTagsFor(body);
		}
	}

	/**
	 * Query libraries.io to find all projects having the given
	 * repository url.
	 * THIS QUERY SHOULD HAVE ITS RESULTS CACHED!
	 * @param body
	 * @return
	 */
	private Technology addPlatform(Technology body) {
		if(body.packageManagerUrl!=null) {
			return addPlatformByPackageManagerUrl(body);
			
		} else {
			Log.errorf("Unable to find a way to get platform of %s", body);
		}
		return body;
	}

	private Technology addPlatformByPackageManagerUrl(Technology body) {
		try {
			URI url = new URI(body.packageManagerUrl);
			String host = url.getHost();
			Optional<String> platformName = getPlatformMappings().entrySet().stream()
				.filter(entry -> host.contains(entry.getKey()))
				.map(Map.Entry::getValue)
				.findFirst();
			if(platformName.isPresent()) {
				body.platform = platformName.get();
				return technologies.update(body, t -> t.platform = platformName.get());
			} else {
				Log.warnf("We couldn't find the platform of %s", body);
				return body;
			}
		} catch (URISyntaxException e) {
			Log.errorf(e, "Unable to create an URI from repository url %s (from technology %s)", e, body.repositoryUrl, body);
			return body;
		}
	}

	private Map<String, String> getPlatformMappings() {
		if(platformMappings==null) {
			platformMappings = createPlatformMappings();
		}
		return platformMappings;		
	}

	private synchronized Map<String, String> createPlatformMappings() {
		List<Platform> platforms = librariesIo.getPlatforms();
		return platforms.stream()
				.filter(p -> !rejectedPlatforms.contains(p.getName()))
				.collect(Collectors.toMap(p -> getDomain(p), Platform::getName));
	}
	
	public String getDomain(Platform p) {
		try {
			URI uri = new URI(p.getHomepage());
			return uri.getHost();
		} catch (URISyntaxException e) {
			throw new RuntimeException("TODO handle URISyntaxException", e);
		}
	}

	private boolean shouldOverrideConfiguration(Exchange exchange1) {
		return overrideMappings;
	}

	private void mergeConfiguration(Exchange exchange1) {
		YamlUpdater.create(
				new File(configurationFolder.toFile(), configurationFile),
				new File(configurationFolder.toFile(), temporaryConfigurationFile))
        .update();
    }
}