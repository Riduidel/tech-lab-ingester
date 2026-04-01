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

import com.zenika.tech.lab.ingester.model.IndicatorRepository;
import com.zenika.tech.lab.ingester.model.Technology;
import com.zenika.tech.lab.ingester.model.TechnologyRepository;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;

/**
 * This test is not a real test, but rather a way for us to have all tags loaded.
 */
@QuarkusTest
@TestProfile(StackOverflowQuestionsIndicatorComputerTest.Configuration.class)
class StackOverflowQuestionsIndicatorComputerTest extends CamelQuarkusTestSupport {
//	@Inject IndicatorRepository indicatorRepository;
	@Inject TechnologyRepository technologyRepository;
	@Inject StackOverflowQuestionsIndicatorComputer tested;
	
	public static class Configuration implements QuarkusTestProfile {
	@Override
	public Map<String, String> getConfigOverrides() {
		return Map.of(
// See https://github.com/apache/camel-quarkus/issues/6882#issuecomment-2567407689
"quarkus.camel.routes-discovery.include-patterns", ".*"+StackOverflowQuestionsIndicatorComputer.ROUTE_NAME
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
                	.to(DirectEndpointBuilderFactory.endpointBuilder("direct", StackOverflowQuestionsIndicatorComputer.ROUTE_NAME))
                	.to("mock:StackOverflowQuestionsIndicatorComputerTest")
                    .end();
            }
        };
    }
	
	@EndpointInject("mock:StackOverflowQuestionsIndicatorComputerTest")
    MockEndpoint mockEndpoint;
//	
//	public boolean containsListOfTags(Exchange exchange) {
//		Object body = exchange.getMessage().getBody();
//		Assertions.assertThat(body)
//			.isInstanceOf(List.class)
//			.asList()
//			.extracting("name")
//			.contains("identification-request")
//			;
//		return true;
//	}

	@Test @Disabled
	void can_load_tags_from_stackoverflow() throws InterruptedException {
		// Given
        mockEndpoint.setExpectedMessageCount(1);
//        mockEndpoint.allMessages()
//        	.predicate(this::containsListOfTags)
//        	;

        // This is reactjs!
        Technology reactjs = technologyRepository.findById(2L);
        // When
		template.sendBody("direct:start", reactjs);
		// Then
		mockEndpoint.assertIsSatisfied();
	}

}
