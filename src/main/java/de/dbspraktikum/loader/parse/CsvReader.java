package de.dbspraktikum.loader.parse;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CsvReader implements AutoCloseable {
    private final BufferedReader reader;
    private final Map<String, Integer> columns;
    private int lineNumber = 1;

    public CsvReader(Path path) throws IOException {
        reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
        List<String> header = CsvUtil.parseFields(reader.readLine());

        columns = new HashMap<>();
        for (int i = 0; i < header.size(); i++) {
            columns.put(header.get(i), i);
        }
    }

    public Row next() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return null;
        }
        lineNumber++;
        return new Row(lineNumber, columns, CsvUtil.parseFields(line));
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    public record Row(int lineNumber, Map<String, Integer> columns, List<String> values) {
        public String value(String name) {
            Integer index = columns.get(name);
            if (index == null || index >= values.size()) {
                return null;
            }

            return values.get(index);
        }
    }
}
