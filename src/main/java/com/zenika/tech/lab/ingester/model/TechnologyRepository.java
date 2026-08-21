package com.zenika.tech.lab.ingester.model;

import com.zenika.tech.lab.ingester.librariesio.model.Project;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class TechnologyRepository implements PanacheRepository<Technology> {

	@Transactional
	public Technology findOrCreateFromLibrariesIOLibrary(Project body) {
		Technology returned = newTechnology(body);
		return findOrCreate(returned);
	}

	private Technology newTechnology(Project body) {
		Technology returned = new Technology();
		returned.name = body.getName();
		returned.description = body.getDescription();
		returned.homepage = body.getHomepage();
		returned.packageManagerUrl = body.getPackageManagerUrl();
		returned.repositoryUrl = body.getRepositoryUrl();
		returned.platform = body.getPlatform();
		return returned;
		
	}

	public Technology findOrCreate(Technology source) {
		Technology returned = null;
		// First find the reference url
		if(source.packageManagerUrl != null) {
			returned = find("packageManagerUrl", source.packageManagerUrl).firstResult();
			if(returned!=null) {
				return returned;
			}
		}
		// If not found, create it and persist it immediatly
		returned = source;
		// We set the id to null because persist will result in an INSERT or UPDATE sql sttatement
		// upon THAT field value
		returned.id = null;
		persist(returned);
		return returned;
	}

}
