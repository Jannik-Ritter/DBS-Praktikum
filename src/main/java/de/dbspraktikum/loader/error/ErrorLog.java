package de.dbspraktikum.loader.error;

import de.dbspraktikum.loader.db.repositories.ErrorRepository;
import de.dbspraktikum.loader.parse.CsvUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public final class ErrorLog implements AutoCloseable {
    private final BufferedWriter writer;
    private final ErrorRepository repository;
    private final Map<String, Integer> counts = new HashMap<>();

    public ErrorLog(Path rejects, ErrorRepository repository) throws IOException {
        this.repository = repository;

        // Directory erstellen, falls es nicht existiert
        Path parent = rejects.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        writer = Files.newBufferedWriter(rejects, StandardCharsets.UTF_8);
        writer.write("Entity,Attribut,Rohwert,Quelle,Meldung");
        writer.newLine();
    }

    public void record(String entity, String attribute, String rawValue, String source, String message) {
        record(new LoadError(entity, attribute, rawValue, source, message));
    }

    public void record(LoadError error) {
        counts.merge(error.message(), 1, Integer::sum);
        
        try {
            writer.write(CsvUtil.formatRow(
                    error.entity(), error.attribute(), error.rawValue(), error.source(), error.message()));
            writer.newLine();
            writer.flush();
            repository.insert(error);
        } catch (IOException | SQLException ex) {
            throw new RuntimeException("Fehler konnte nicht protokolliert werden", ex);
        }
    }

    public Map<String, Integer> counts() {
        return counts;
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
