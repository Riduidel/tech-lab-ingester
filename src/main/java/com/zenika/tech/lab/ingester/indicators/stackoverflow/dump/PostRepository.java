package com.zenika.tech.lab.ingester.indicators.stackoverflow.dump;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.zenika.tech.lab.ingester.indicators.stackoverflow.StackOverflowQuestionsIndicatorComputer;
import com.zenika.tech.lab.ingester.indicators.stackoverflow.model.Tag;
import com.zenika.tech.lab.ingester.model.Indicator;
import com.zenika.tech.lab.ingester.model.Technology;

import io.agroal.api.AgroalDataSource;
import io.quarkus.hibernate.orm.PersistenceUnit;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PostRepository implements PanacheRepository<Post>{
	@ConfigProperty(name = "tech-lab-ingester.indicators.stackexchange.questions.sql")
	String groupQuestionsByMonthSql;
    @Inject
    @PersistenceUnit("stackoverflow")
    EntityManager entityManager;
    @Inject @io.quarkus.agroal.DataSource("stackoverflow")
    AgroalDataSource ds;

	private Indicator toQuestionIndicator(Technology technology, Object[] row) {
		LocalDate localDate = LocalDate.of(Integer.parseInt(row[0].toString()),
				Integer.parseInt(row[1].toString()), 1);
		Date d = Date.from(localDate.atStartOfDay(ZoneId.of("UTC")).toInstant());
		return new Indicator(
				technology,
				StackOverflowQuestionsIndicatorComputer.INDICATOR_ID,
				d,
				row[2].toString()
		);
	}
	// Aggregating all StackOverflow questions is damn slow
	@TransactionConfiguration(timeout = 60*10)
	@Transactional
	public List<Indicator> groupQuestionsByMonth(Technology body, Tag tag, Date startDate, Date endDate) {
		String start = toString(startDate);
		String end = toString(endDate);
		Query extractionQuery = entityManager.createNativeQuery(groupQuestionsByMonthSql)
				.setParameter("tag", String.format("%%<%s>%%",  tag.name))
				.setParameter("start", start)
				.setParameter("end", end);
		List<Object[]> results = extractionQuery.getResultList();
		return results.stream()
				.map(row -> toQuestionIndicator(body, row))
				.toList();
		
	}
	/**
	 * SimpleDateFormat seems to believe 2021-01-01 is 2020-01-01 (they have reasons)
	 * but I know they're not the same!
	 */
	private String toString(Date date) {
		return new SimpleDateFormat("yyyy-MM-dd").format(date);
	}

}
