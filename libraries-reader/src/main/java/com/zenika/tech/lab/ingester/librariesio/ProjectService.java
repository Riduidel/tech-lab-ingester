package com.zenika.tech.lab.ingester.librariesio;

import com.zenika.tech.lab.ingester.IProjectService;
import com.zenika.tech.lab.ingester.librariesio.model.Project;

public class ProjectService implements IProjectService {

    private final Project project;

    public ProjectService(Project project) {
        this.project = project;
    }

    @Override
    public String getPackageManagerUrl() {
        return project.getPackageManagerUrl();
    }

    @Override
    public String getName() {
        return project.getName();
    }

    @Override
    public String getDescription() {
        return project.getDescription();
    }

    @Override
    public String getHomepage() {
        return project.getHomepage();
    }

    @Override
    public String getPlatform() {
        return project.getPlatform();
    }

    @Override
    public String getRepositoryUrl() {
        return project.getRepositoryUrl();
    }
}
