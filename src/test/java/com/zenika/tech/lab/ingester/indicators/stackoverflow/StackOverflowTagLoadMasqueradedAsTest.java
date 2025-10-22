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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;

/**
 * This test is not a real test, but rather a way for us to have all tags loaded.
 */
@Disabled
@QuarkusTest
@TestProfile(StackOverflowTagLoadMasqueradedAsTest.Configuration.class)
class StackOverflowTagLoadMasqueradedAsTest extends CamelQuarkusTestSupport {
	
	@Inject TagService tags;
	
	public static class Configuration implements QuarkusTestProfile {
	@Override
	public Map<String, String> getConfigOverrides() {
		// A stackexchange server with few tags
		return Map.of(
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
                	.to("mock:StackOverflowTagLoadMasqueradedAsTest")
                    .end();
            }
        };
    }
	
	@EndpointInject("mock:StackOverflowTagLoadMasqueradedAsTest")
    MockEndpoint mockEndpoint;

	@Test
	void can_load_tags_from_stackoverflow() throws InterruptedException {
		// Given
        mockEndpoint.setExpectedMessageCount(1);
        mockEndpoint.allMessages()
        	.predicate(this::performAsserts)
        	;

        // When
		template.sendBody("direct:start", null);
		// Then
		mockEndpoint.assertIsSatisfied();
	}
	
	public boolean performAsserts(Exchange exchange) {
		Object body = exchange.getMessage().getBody();
		Assertions.assertThat(body)
			.isInstanceOf(List.class)
			.asList()
			.extracting("name")
			.contains("identification-request")
			;
		return true;
	}

}
