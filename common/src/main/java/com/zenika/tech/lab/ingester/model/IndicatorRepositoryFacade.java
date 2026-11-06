package com.zenika.tech.lab.ingester.model;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.Date;

public class IndicatorRepositoryFacade {

	private final IndicatorRepository repository;
	private final String indicatorName;

	public IndicatorRepositoryFacade(IndicatorRepository repository, String indicatorName) {
		this.repository = repository;
		this.indicatorName = indicatorName;
	}

	public boolean hasIndicatorForMonth(Technology technology, Date startOfMonth) {
		return repository.hasIndicatorForMonth(technology, indicatorName, startOfMonth);
	}

	public void saveIndicator(Technology technology, String value) {
		repository.saveIndicator(technology, indicatorName, value);
	}

	public boolean maybePersist(Indicator indicator) {
		return repository.maybePersist(indicator);
	}

	public Collection<Indicator> findAll(Technology t) {
		return repository.findAll(t, indicatorName);
	}

}
