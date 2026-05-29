package de.dbspraktikum.loader.parse;

import de.dbspraktikum.loader.error.ErrorLog;
import de.dbspraktikum.loader.error.Errors;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class ValueParser {
    private final ErrorLog errors;

    public ValueParser(ErrorLog errors) {
        this.errors = errors;
    }

    public Integer positiveInt(String raw, String entity, String attribute, String source) {
        Integer value = integer(raw, entity, attribute, source);
        if (value == null) {
            return null;
        }
        if (value <= 0) {
            errors.record(entity, attribute, raw, source, Errors.VALUE_MUST_BE_POSITIVE);
            return null;
        }
        return value;
    }

    public Integer nonNegativeInt(String raw, String entity, String attribute, String source) {
        Integer value = integer(raw, entity, attribute, source);
        if (value == null) {
            return null;
        }
        if (value < 0) {
            errors.record(entity, attribute, raw, source, Errors.VALUE_MUST_NOT_BE_NEGATIVE);
            return null;
        }
        return value;
    }

    public Integer integer(String raw, String entity, String attribute, String source) {
        String cleaned = TextUtil.clean(raw);
        if (cleaned == null) {
            return null;
        }
        try {
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException ex) {
            errors.record(entity, attribute, raw, source, Errors.VALUE_NOT_INTEGER);
            return null;
        }
    }

    public BigDecimal decimal(String raw, String entity, String attribute, String source) {
        String cleaned = TextUtil.clean(raw);
        if (cleaned == null) {
            return null;
        }
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException ex) {
            errors.record(entity, attribute, raw, source, Errors.VALUE_NOT_NUMBER);
            return null;
        }
    }

    public LocalDate date(String raw, String entity, String attribute, String source) {
        String cleaned = TextUtil.clean(raw);
        if (cleaned == null) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(cleaned);
            if (date.isAfter(LocalDate.now())) {
                errors.record(entity, attribute, raw, source, Errors.DATE_IN_FUTURE);
                return null;
            }
            return date;
        } catch (RuntimeException ex) {
            errors.record(entity, attribute, raw, source, Errors.DATE_INVALID_FORMAT);
            return null;
        }
    }

    public BigDecimal price(String rawPrice, String rawMultiplier, String source) {
        String cleanedPrice = TextUtil.clean(rawPrice);
        if (cleanedPrice == null) {
            return null;
        }
        try {
            BigDecimal price = new BigDecimal(cleanedPrice);
            BigDecimal multiplier = TextUtil.clean(rawMultiplier) == null ? BigDecimal.ONE : new BigDecimal(rawMultiplier);
            BigDecimal result = price.multiply(multiplier);
            if (result.signum() < 0) {
                errors.record("Konditionen", "Preis", cleanedPrice, source, Errors.PRICE_MUST_NOT_BE_NEGATIVE);
                return null;
            }
            return result;
        } catch (NumberFormatException ex) {
            errors.record("Konditionen", "Preis", cleanedPrice, source, Errors.PRICE_NOT_VALID_NUMBER);
            return null;
        }
    }
}
