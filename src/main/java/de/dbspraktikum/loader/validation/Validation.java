package de.dbspraktikum.loader.validation;

import de.dbspraktikum.loader.parse.TextUtil;

import java.util.Locale;
import java.util.regex.Pattern;

public final class Validation {
    private static final Pattern ASIN_PATTERN = Pattern.compile("[A-Z0-9]{10}");
    private static final Pattern EAN13_PATTERN = Pattern.compile("[0-9]{13}");
    private static final Pattern ISBN10_PATTERN = Pattern.compile("[0-9]{9}[0-9X]");
    private static final Pattern CURRENCY_PATTERN = Pattern.compile("[A-Z]{3}");

    private Validation() {
    }

    public static String normalizeAsin(String value) {
        String cleaned = TextUtil.clean(value);
        return cleaned == null ? null : cleaned.toUpperCase(Locale.ROOT);
    }

    public static boolean validAsin(String value) {
        return value != null && ASIN_PATTERN.matcher(value).matches();
    }

    public static boolean validEan13(String value) {
        if (value == null || !EAN13_PATTERN.matcher(value).matches()) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = value.charAt(i) - '0';
            sum += i % 2 == 0 ? digit : digit * 3;
        }
        int checkDigit = (10 - (sum % 10)) % 10;
        return checkDigit == value.charAt(12) - '0';
    }

    public static boolean validIsbn10(String value) {
        if (value == null || !ISBN10_PATTERN.matcher(value).matches()) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 10; i++) {
            char character = value.charAt(i);
            int digit = character == 'X' ? 10 : character - '0';
            sum += (10 - i) * digit;
        }
        return sum % 11 == 0;
    }

    public static boolean validCurrencyCode(String value) {
        return value != null && CURRENCY_PATTERN.matcher(value).matches() && "EUR".equals(value);
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
