package de.dbspraktikum.loader.validation;

import de.dbspraktikum.loader.parse.TextUtil;

import java.util.Locale;
import java.util.regex.Pattern;

public final class Validation {
    private static final Pattern ASIN_PATTERN = Pattern.compile("[A-Z0-9]{10}");

    private Validation() {
    }

    public static String normalizeAsin(String value) {
        String cleaned = TextUtil.clean(value);
        return cleaned == null ? null : cleaned.toUpperCase(Locale.ROOT);
    }

    public static boolean validAsin(String value) {
        return value != null && ASIN_PATTERN.matcher(value).matches();
    }

    public static String mapProductType(String pgroup) {
        if (pgroup == null) {
            return null;
        }
        return switch (pgroup.toLowerCase(Locale.ROOT)) {
            case "book", "buch" -> "Buch";
            case "music", "musical" -> "Musik-CD";
            case "dvd" -> "DVD";
            default -> null;
        };
    }
}
