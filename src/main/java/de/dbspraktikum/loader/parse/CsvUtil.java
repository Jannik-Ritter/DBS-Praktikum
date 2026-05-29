package de.dbspraktikum.loader.parse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class CsvUtil {
    private CsvUtil() {
    }

    public static String escapeField(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    public static List<String> parseFields(String line) {
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

    public static String formatRow(String... fields) {
        return Arrays.stream(fields)
                .map(CsvUtil::escapeField)
                .collect(Collectors.joining(","));
    }
}
