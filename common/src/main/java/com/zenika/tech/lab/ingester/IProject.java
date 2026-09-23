package com.zenika.tech.lab.ingester;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public interface IProject {
    @JsonProperty("code_of_conduct_url")
    String getCodeOfConductUrl();

    @JsonProperty("contributions_count")
    Integer getContributionsCount();

    @JsonProperty("contribution_guidelines_url")
    String getContributionGuidelinesUrl();

    @JsonProperty("dependent_repos_count")
    Integer getDependentReposCount();

    @JsonProperty("dependents_count")
    Integer getDependentsCount();

    @JsonProperty("deprecation_reason")
    Object getDeprecationReason();

    @JsonProperty("description")
    String getDescription();

    @JsonProperty("forks")
    Integer getForks();

    @JsonProperty("funding_urls")
    List<String> getFundingUrls();

    @JsonProperty("homepage")
    String getHomepage();

    @JsonProperty("keywords")
    List<String> getKeywords();

    @JsonProperty("language")
    String getLanguage();

    @JsonProperty("latest_download_url")
    String getLatestDownloadUrl();

    @JsonProperty("name")
    String getName();

    @JsonProperty("package_manager_url")
    String getPackageManagerUrl();

    @JsonProperty("platform")
    String getPlatform();

    @JsonProperty("repository_url")
    String getRepositoryUrl();
}
