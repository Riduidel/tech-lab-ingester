package com.zenika.tech.lab.ingester.indicators.stackoverflow;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.builder.endpoint.dsl.DirectEndpointBuilderFactory.DirectEndpointBuilder;
import org.apache.camel.support.processor.idempotent.MemoryIdempotentRepository;
import org.threeten.bp.Instant;

import com.zenika.tech.lab.ingester.indicators.IndicatorComputer;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.dump.PostRepository;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.model.KnownTechnologiesRepository;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.model.KnownTechnology;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.model.Tag;
import com.zenika.tech.lab.ingester.model.Indicator;
import com.zenika.tech.lab.ingester.model.IndicatorNamed;
import com.zenika.tech.lab.ingester.model.IndicatorRepositoryFacade;
import com.zenika.tech.lab.ingester.model.Technology;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class StackOverflowQuestionsIndicatorComputer extends EndpointRouteBuilder implements IndicatorComputer {
	public static final String STACKOVERFLOW_QUESTIONS_COUNT = "stackoverflow.questions.count";
	public static final String ROUTE_NAME = "compute-"+STACKOVERFLOW_QUESTIONS_COUNT.replace('.', '-');
	public static final String INDICATOR_ID = "stackoverflow.questions";
	@Inject PostRepository postRepository;
	@Inject KnownTechnologiesRepository knownTechnologiesRepository;
	@Inject @IndicatorNamed(INDICATOR_ID) IndicatorRepositoryFacade indicators;

	private @Inject TagService tagsService;
	private DirectEndpointBuilder getFromRoute() {
		return direct(ROUTE_NAME);
	}

	@Override
	public String getFromRouteName() {
		return getFromRoute().getRawUri();
	}

	@Override
	public void configure() throws Exception {
		from(getFromRoute())
			.routeId(ROUTE_NAME)
			.idempotentConsumer()
				.body(Technology.class, t -> String.format("%s-%s", STACKOVERFLOW_QUESTIONS_COUNT, t.packageManagerUrl))
				.idempotentRepository(MemoryIdempotentRepository.memoryIdempotentRepository(10*2))
			.process(this::countQuestions)
			.end()
		;
	}

	private void countQuestions(Exchange exchange1) {
		countQuestions(exchange1.getMessage().getBody(Technology.class));
	}

	public void countQuestions(Technology body) {
		// First step, get the KnownTechnology object from the Technology one
		// Then check if that technology has associated tags
		// If tags are presents, get the StackOverflow associated tag
		// Check if we already have indicators computed (to fasten the query)
		// Run the query on missing data
		// Persist result
		knownTechnologiesRepository.findByTechnology(body)
			.ifPresent(known -> countQuestions(body, known));
	}

	private void countQuestions(Technology body, KnownTechnology known) {
		if(known.linkedTags.isEmpty()) {
			return;
		} else {
			known.linkedTags.stream()
				.filter(tag -> tag.site.equalsIgnoreCase("stackoverflow"))
				.findFirst()
				.ifPresent(tag -> countQuestions(known, tag));
		}
	}

	
	private void countQuestions(KnownTechnology known, Tag tag) {
		Comparator<Indicator> c = Comparator.comparing(Indicator::getDate);
		SortedSet<Indicator> knownIndicators = indicators.findAll(known.id.technology)
				.stream()
				.collect(Collectors.toCollection(() -> new TreeSet<Indicator>(c)))
				;
		// So now we have the known indicators, let's check if we can have some sensible values
		Date stackOverflowDataDumpStart = new Date(2008-1900, 1-1, 1);
		Date stackOverflowDataDumpEnd = new Date(2024-1900, 2-1, 31);
		Date startDate = null, endDate = null;
		if(knownIndicators.isEmpty()) {
			startDate = stackOverflowDataDumpStart;
			endDate = stackOverflowDataDumpEnd;
		} else {
			// Here we have the dates of the data dump
			Date knownStartDate = knownIndicators.stream().findFirst().map(Indicator::getDate).orElse(stackOverflowDataDumpStart);
			Date knownEndDate = knownIndicators.reversed().stream().findFirst().map(Indicator::getDate).orElse(stackOverflowDataDumpEnd);
			if(knownEndDate.compareTo(stackOverflowDataDumpEnd)<0) {
				startDate = knownEndDate;
				endDate = stackOverflowDataDumpEnd;
			} else {
				Log.infof(" We have already counted stackoverflow questions for %s tag %s", known.getTechnologyName(), tag.name);
			}
		}
		Log.infof("📥 Counting stackoverflow questions for %s tag %s", known.getTechnologyName(), tag.name);
		// Convert to LocalDate for duration conversion
		LocalDate startLocal= startDate.toInstant()
			      .atZone(ZoneOffset.UTC)
			      .toLocalDate();
		LocalDate endLocal=endDate.toInstant()
			      .atZone(ZoneOffset.UTC)
			      .toLocalDate();

		countQuestionsBetween(known, tag, startLocal, endLocal);
		Log.infof("📥 Counted stackoverflow questions for %s tag %s", known.getTechnologyName(), tag.name);
	}

	private void countQuestionsBetween(KnownTechnology known, Tag tag, LocalDate startLocalDate, LocalDate endLocalDate) {
		LocalDate current=startLocalDate.withDayOfMonth(1);
		// Directly aggregating all questions generate far too big transactions
		// Let's split that year by year
		do {
			Date startDate = Date.from(current.atStartOfDay(ZoneOffset.UTC).toInstant());
			current = current.plusMonths(1).withDayOfMonth(1);
			if(current.isAfter(endLocalDate))
				current = endLocalDate;
			Date endDate = Date.from(current.atStartOfDay(ZoneOffset.UTC).toInstant());
			DateFormat FORMAT = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);
			Log.infof("📥 Counting stackoverflow questions for %s tag %s between %s and %s", known.getTechnologyName(), tag.name, 
					FORMAT.format(startDate),
					FORMAT.format(endDate));
			postRepository.groupQuestionsByMonth(known.id.technology, tag, startDate, endDate)
				.forEach(indicators::maybePersist);
		} while(current.isBefore(endLocalDate));
	}

	@Override
	public boolean canCompute(Technology technology) {
		return tagsService.hasTagsFor(technology);
	}

}
