package com.zenika.tech.lab.ingester.indicators.stackoverflow.dump;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class Configuration implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
        );
    }

    @Override
    public String getConfigProfile() {
        return "test";
    }
}