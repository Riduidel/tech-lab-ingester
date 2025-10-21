package com.zenika.tech.lab.ingester;

import java.util.Map;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.endpoint.dsl.DirectEndpointBuilderFactory;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.quarkus.test.CamelQuarkusTestSupport;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(AddMissingFieldsTest.Configuration.class)
class AddMissingFieldsTest extends CamelQuarkusTestSupport {

	public static class Configuration implements QuarkusTestProfile {
		@Override
		public Map<String, String> getConfigOverrides() {
			// A stackexchange server with few tags
			return Map.of(
					// See
					// https://github.com/apache/camel-quarkus/issues/6882#issuecomment-2567407689
					"quarkus.camel.routes-discovery.include-patterns", ".*" + AddMissingFields.class.getSimpleName());
		}
	}

	@Override
	protected RoutesBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from("direct:start")
					.log("Activate only the route to test")
					.to(DirectEndpointBuilderFactory.endpointBuilder("direct", AddMissingFields.class.getSimpleName()))
					.to("mock:AddMissingFieldsTest").end();
			}
		};
	}

	@EndpointInject("mock:AddMissingFieldsTest")
	MockEndpoint mockEndpoint;

	@Test
	void can_find_associated_tags() throws InterruptedException {
		// Given
		mockEndpoint.setExpectedMessageCount(1);
		mockEndpoint.allMessages().predicate(this::performAssertions);

		// When
		template.sendBody("direct:start", null);
		// Then
		mockEndpoint.assertIsSatisfied();
	}

	public boolean performAssertions(Exchange exchange) {
		Object body = exchange.getMessage().getBody();
		return true;
	}
}
