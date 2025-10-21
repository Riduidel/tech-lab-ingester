package com.zenika.tech.lab.ingester.indicators.stackoverflow;

import java.util.List;
import java.util.Map;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.endpoint.dsl.DirectEndpointBuilderFactory;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.quarkus.test.CamelQuarkusTestSupport;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Assumptions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;

/**
 * We need to have a database started (because this will trigger database writes that I want to cache).
 * This is why this test is a full blown quarkus test
 */
@QuarkusTest
@TestProfile(TagLoaderSmallTest.Configuration.class)
class TagLoaderSmallTest extends CamelQuarkusTestSupport {
	
	@Inject TagService tags;
	
	public static class Configuration implements QuarkusTestProfile {
	@Override
	public Map<String, String> getConfigOverrides() {
		// A stackexchange server with few tags
		return Map.of(
				"tech-lab-ingester.indicators.stackexchange.tags.sites", "Literature",
// See https://github.com/apache/camel-quarkus/issues/6882#issuecomment-2567407689
"quarkus.camel.routes-discovery.include-patterns", ".*"+TagLoader.class.getSimpleName()
				);
	}
	}
	
	@Override
	protected RoutesBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                	.log("Activate only the route to test")
                	.to(DirectEndpointBuilderFactory.endpointBuilder("direct", TagLoader.class.getSimpleName()))
                	.to("mock:TagLoaderSmallTest")
                    .end();
            }
        };
    }
	
	@EndpointInject("mock:TagLoaderSmallTest")
    MockEndpoint mockEndpoint;
	
	public boolean containsListOfTags(Exchange exchange) {
		Object body = exchange.getMessage().getBody();
		Assertions.assertThat(body)
			.isInstanceOf(List.class)
			.asList()
			.extracting("name")
			.contains("identification-request")
			;
		return true;
	}

	@Test
	void can_load_tags_from_stackoverflow() throws InterruptedException {
		// Given
        mockEndpoint.setExpectedMessageCount(1);
        mockEndpoint.allMessages()
        	.predicate(this::containsListOfTags)
        	;

        // When
		template.sendBody("direct:start", null);
		// Then
		mockEndpoint.assertIsSatisfied();
	}
}
