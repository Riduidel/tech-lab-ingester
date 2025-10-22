package com.zenika.tech.lab.ingester.indicators.stackoverflow;

import java.util.Optional;

import com.zenika.tech.lab.ingester.model.Technology;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class KnownTechnologiesRepository implements PanacheRepository<KnownTechnology>  {

	public boolean isKnown(Technology body) {
		Optional<KnownTechnology> found = findByTechnology(body);
		return found.map(k -> k.known).orElse(false);
	}

	public Optional<KnownTechnology> findByTechnology(Technology body) {
		return find("id.technology=?1", body).firstResultOptional();
	}

}
