package com.zenika.tech.lab.ingester.indicators.stackoverflow.api;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.Separator;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.api.entities.StackExchangeList;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.api.entities.TagDefinition;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.api.entities.TagWiki;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.api.entities.Total;

import io.quarkus.rest.client.reactive.ClientQueryParam;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

@RateLimit(value = 30, window = 1, windowUnit = ChronoUnit.SECONDS)
@RegisterRestClient(configKey = "stackexchange")
@ClientQueryParam(name="key", value="${tech-lab-ingester.stackexchange.api.key}")
@Path("/2.3")
public interface StackExchangeClient {
	/**
	 * @see https://api.stackexchange.com/docs/tags
	 */
	@GET @Path("/tags")
	// TODO make this filter configurable
	@ClientQueryParam(name = "filter", value = "!6N4UX.)7jZzX1")
	public StackExchangeList<TagDefinition> getTags(
			@QueryParam("site") String site,
			@QueryParam("page") int page, 
			@QueryParam("pagesize") int pagesize);
	
	public default long getTagsCount(String site) {
		return ___do_not_call_outside_interface___doGetCount(site).total();
	}

	/**
	 * @see https://api.stackexchange.com/docs/tags
	 */
	@GET @Path("/tags")
	// We have to make sure the last activity date is tested
	@ClientQueryParam(name = "filter", value = "total") 
	public Total ___do_not_call_outside_interface___doGetCount(
			@QueryParam("site") String site);

	default public StackExchangeList<TagWiki> getTagsWikis(String site, 
			List<String> tags, 			
			int page, 
			int pagesize) {
		return getTagsWikis(site, 
				tags.stream().collect(Collectors.joining(";")), 
				page, pagesize);
	}

	/**
	 * @see https://api.stackexchange.com/docs/wikis-by-tags
	 */
	@GET @Path("/tags/{tags}/wikis")
	// We have to make sure the whole body is tested
	@ClientQueryParam(name = "filter", value = "!nNPvSNMavg") 
	public StackExchangeList<TagWiki> getTagsWikis(
			@QueryParam("site") String site, 
			@PathParam("tags") String tags, 			
			@QueryParam("page") int page, 
			@QueryParam("pagesize") int pagesize);
}
