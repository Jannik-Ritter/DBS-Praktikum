package de.dbspraktikum.loader.error;

public final class Errors {
    private Errors() {
    }

    public static final String FILE_NOT_FOUND = "Datei nicht gefunden";

    public static final String INVALID_ASIN = "Ungueltige ASIN";
    public static final String ASIN_MISSING_OR_INVALID = "ASIN fehlt oder hat kein gueltiges Format";
    public static final String INVALID_PRODUCT_NUMBER = "Ungueltige Produktnummer";
    public static final String PRODUCT_NOT_FOUND = "Produkt existiert nicht";
    public static final String UNKNOWN_PRODUCT_TYPE = "Unbekannter Produkttyp";

    public static final String SIMILAR_PRODUCT_NOT_FOUND = "Aehnliches Produkt existiert nicht";
    public static final String SELF_SIMILAR_PRODUCT = "Produkt darf nicht sich selbst aehnlich sein";

    public static final String DUPLICATE_REVIEW = "Doppelte Rezension";
    public static final String REVIEWER_MISSING = "Rezensent fehlt";
    public static final String RATING_OUT_OF_RANGE = "Bewertung muss zwischen 1 und 5 liegen";

    public static final String VALUE_MUST_BE_POSITIVE = "Wert muss groesser als 0 sein";
    public static final String VALUE_MUST_NOT_BE_NEGATIVE = "Wert darf nicht negativ sein";
    public static final String VALUE_NOT_INTEGER = "Wert ist keine ganze Zahl";

    public static final String DATE_IN_FUTURE = "Datum darf nicht in der Zukunft liegen";
    public static final String DATE_INVALID_FORMAT = "Datum ist nicht im Format YYYY-MM-DD";

    public static final String PRICE_MUST_NOT_BE_NEGATIVE = "Preis darf nicht negativ sein";
    public static final String PRICE_NOT_VALID_NUMBER = "Preis ist keine gueltige Zahl";

    public static final String CATEGORY_NAME_MISSING = "Kategoriename fehlt";
    public static final String REQUIRED_FIELD_MISSING = "Pflichtfeld fehlt";

    public static String productTypeMismatch(String existingType) {
        return "Produkt wurde bereits mit anderem Typ geladen: " + existingType;
    }
}
