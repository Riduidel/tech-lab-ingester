package com.zenika.tech.lab.ingester.indicators.stackoverflow.dump;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@QuarkusTestResource(SqliteDatabaseResource.class)
public class SqliteDatabaseResource implements QuarkusTestResourceLifecycleManager {

    @Override
    public Map<String, String> start() {
        Config config = ConfigProvider.getConfig();

        String sourceDatabaseFilePath = config.getValue("database_file.source-path", String.class);
        String runtimeDatabaseFilePath = config.getValue("database_file.runtime-path", String.class);

        Path sourceFilePath = Path.of(sourceDatabaseFilePath);
        Path runtimeFilePath = Path.of(runtimeDatabaseFilePath);

        copyDatabaseFile(sourceFilePath, runtimeFilePath);
        return Map.of();
    }

    private static void copyDatabaseFile(Path sourceFilePath, Path runtimeFilePath) {
        try {
            if (sourceFilePath.getParent() != null) {
                Files.createDirectories(sourceFilePath.getParent());
            }
            if (sourceFilePath.getParent() != null) {
                Files.createDirectories(sourceFilePath.getParent());
            }
            if (Files.exists(sourceFilePath)) {
                Files.copy(sourceFilePath, runtimeFilePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database from" + sourceFilePath, e);
        }
    }

    @Override
    public void stop() {
    }
}

