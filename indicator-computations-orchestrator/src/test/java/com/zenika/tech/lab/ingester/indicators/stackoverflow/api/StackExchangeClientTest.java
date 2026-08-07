package com.zenika.tech.lab.ingester.indicators.stackoverflow.api;

import java.util.Arrays;

import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.quarkus.test.CamelQuarkusTestSupport;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.api.entities.StackExchangeList;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.api.entities.TagDefinition;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.api.entities.TagWiki;

import io.quarkus.test.junit.QuarkusTest;

/**
 * We voluntary test the stack exchange client on a non-it stackexchange site 
 * (to avoid consuming our tokens bucket)
 */
@QuarkusTest
class StackExchangeClientTest extends CamelQuarkusTestSupport {
	
	@Override
	protected RoutesBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                	.log("Turning off all routes to avoid side effects")
                    .end();
            }
        };
    }
	
	@RestClient StackExchangeClient client;
	
	@Test void can_have_tag_count() {
		Assertions.assertThat(client.getTagsCount("scifi"))
			.isGreaterThan(1000L);
	}

	@Test
	void can_get_list_of_tags() {
		// When
		StackExchangeList<TagDefinition> tags = client.getTags("scifi", 1, 100);
		// Then
		SoftAssertions.assertSoftly(assertions -> {
			assertions.assertThat(tags.hasMore())
				.describedAs("There are more than 100 tags on scifi site")
				.isTrue();
			assertions.assertThat(tags.quotaMax())
				.describedAs("max quota is standard one")
				.isEqualTo(10_000);
			assertions.assertThat(tags.quotaRemaining())
				.describedAs("We should have some quota remaining")
				.isPositive();
			assertions.assertThat(tags.items())
				.describedAs("We got some tags")
				.isNotEmpty();
		});
	}

	@Test
	void can_get_list_of_tags_wikis() {
		// When
		StackExchangeList<TagWiki> tags = client.getTagsWikis("scifi", 
				Arrays.asList("marvel", "story-identification"), 1, 100);
		// Then
		SoftAssertions.assertSoftly(assertions -> {
			assertions.assertThat(tags.hasMore())
				.describedAs("There are only two wiki pages given")
				.isFalse();
			assertions.assertThat(tags.quotaMax())
				.describedAs("max quota is standard one")
				.isEqualTo(10_000);
			assertions.assertThat(tags.quotaRemaining())
				.describedAs("We should have some quota remaining")
				.isPositive();
			assertions.assertThat(tags.items())
				.describedAs("We got some tags")
				.isNotEmpty();
			assertions.assertThat(tags.items())
				.extracting(t -> t.tagName())
				.asList()
				.contains("marvel", "story-identification");
		});
	}

}
