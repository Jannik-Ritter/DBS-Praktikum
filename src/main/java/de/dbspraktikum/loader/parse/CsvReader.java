package de.dbspraktikum.loader.parse;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CsvReader implements AutoCloseable {
    private final BufferedReader reader;
    private final Map<String, Integer> columns;
    private int lineNumber = 1;

    public CsvReader(Path path) throws IOException {
        reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
        List<String> header = parseLine(reader.readLine());
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
        return new Row(lineNumber, columns, parseLine(line));
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    private static List<String> parseLine(String line) {
        if (line == null) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        values.add(current.toString());
        return values;
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
