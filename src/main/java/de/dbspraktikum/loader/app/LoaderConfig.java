package de.dbspraktikum.loader.app;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public record LoaderConfig(
        Path dataDir,
        Path schema,
        Path rejects,
        String dbUrl,
        String dbUser,
        String dbPassword
) {
    private static final String DEFAULT_DATA_DIR = "data";
    private static final String DEFAULT_SCHEMA = "src/dbs-schema.sql";
    private static final String DEFAULT_REJECTS = "build/rejected-records.csv";

    private static final String DEFAULT_DB_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String DEFAULT_USER = "postgres";
    private static final String DEFAULT_PASSWORD = "test";

    public static LoaderConfig from(String[] args) throws IOException {
        Map<String, String> parsed = parseArgs(args);
        Properties properties = loadProperties(parsed);

        Path dataDir = Path.of(value(parsed, properties, "--data-dir", "data.dir", DEFAULT_DATA_DIR));
        String schemaValue = value(parsed, properties, "--schema", "schema", DEFAULT_SCHEMA);
        Path schema = "none".equalsIgnoreCase(schemaValue) ? null : Path.of(schemaValue);
        Path rejects = Path.of(value(parsed, properties, "--rejects", "rejects", DEFAULT_REJECTS));

        String dbUrl = value(parsed, properties, "--db-url", "db.url", env("DB_URL", DEFAULT_DB_URL));
        String dbUser = value(parsed, properties, "--db-user", "db.user", env("DB_USER", DEFAULT_USER));
        String dbPassword = value(parsed, properties, "--db-password", "db.password", env("DB_PASSWORD", DEFAULT_PASSWORD));

        return new LoaderConfig(dataDir, schema, rejects, dbUrl, dbUser, dbPassword);
    }

    private static Properties loadProperties(Map<String, String> parsed) throws IOException {
        Properties properties = new Properties();
        Path propertiesPath = parsed.containsKey("--config") ? Path.of(parsed.get("--config")) : Path.of("loader.properties");

        if (Files.exists(propertiesPath)) {
            try (InputStream input = Files.newInputStream(propertiesPath)) {
                properties.load(input);
            }
        }

        return properties;
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> parsed = new LinkedHashMap<>();
        ArrayDeque<String> remaining = new ArrayDeque<>(List.of(args));

        while (!remaining.isEmpty()) {
            String key = remaining.removeFirst();
            if (!key.startsWith("--")) {
                throw new IllegalArgumentException("Unbekanntes Argument: " + key);
            }
            if (remaining.isEmpty()) {
                throw new IllegalArgumentException("Wert fehlt fuer Argument: " + key);
            }
            parsed.put(key, remaining.removeFirst());
        }

        return parsed;
    }

    private static String value(Map<String, String> args, Properties properties, String argName, String propertyName, String defaultValue) {
        return args.getOrDefault(argName, properties.getProperty(propertyName, defaultValue));
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
