package com.zenika.tech.lab.ingester.indicators.stackoverflow;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.routepolicy.quartz.SimpleScheduledRoutePolicy;

/**
 * Load tags from configured StackExchange sites.
 */
public class StackOverflowTagLoaderRouteBuilder extends EndpointRouteBuilder {

	@Override
	public void configure() throws Exception {
		SimpleScheduledRoutePolicy policy = new SimpleScheduledRoutePolicy();
		
		LocalDateTime nextfirstofmonthweekday = LocalDateTime.now();
		if(nextfirstofmonthweekday.getDayOfMonth()!=1) {
			nextfirstofmonthweekday = nextfirstofmonthweekday.plusMonths(1);
			nextfirstofmonthweekday = nextfirstofmonthweekday.with(TemporalAdjusters.firstDayOfMonth());
		} else {
			nextfirstofmonthweekday = nextfirstofmonthweekday.plus(10, ChronoUnit.MINUTES);
		}
		Date next = new Date(nextfirstofmonthweekday.toInstant(ZoneOffset.UTC).getEpochSecond()*1000);
		policy.setRouteStartDate(next);
		policy.setRouteStartRepeatCount(1);
		// Millisecond interval between restarts
		// I aim for more than one week, so
		policy.setRouteStartRepeatInterval(1000*60*60*24*7);
		
		from(timer("autostart").repeatCount(1))
			.routePolicy(policy)
			.autoStartup(false)
			.id("stackexchange-tag-loader-starter")
			.log("🚀 Starting reading of StackExchange sites")
			.to(seda(TagLoader.class.getSimpleName()))
			.log("🏁 All StackExchange tags should be loaded now")
			;
	}

}
