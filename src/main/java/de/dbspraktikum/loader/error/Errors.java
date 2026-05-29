package de.dbspraktikum.loader.error;

public final class Errors {
    private Errors() {
    }

    // Syntaxfehler
    public static final String FILE_NOT_FOUND = "Datei nicht gefunden";
    public static final String INVALID_PRODUCT_NUMBER = "Produktnummer fehlt oder hat kein gueltiges Format";
    public static final String EAN_INVALID_FORMAT = "EAN hat ungültiges Format";
    public static final String ISBN_INVALID_FORMAT = "ISBN hat ungültiges Format";
    public static final String CURRENCY_INVALID_FORMAT = "Währung hat ungültiges Format";
    public static final String DATE_INVALID_FORMAT = "Datum ist nicht im Format YYYY-MM-DD";

    // Duplikatsätze
    public static final String DUPLICATE_REVIEW = "Doppelte Rezension"; // Ein Nutzer kann nur maximal eine Rezension pro Produkt abgeben
    public static final String DUPLICATE_PRODUCT_SUBTYPE = "Doppelter Produkttyp-Datensatz";
    public static final String DUPLICATE_TRACK = "Doppeltes Lied";
    public static final String DUPLICATE_PRODUCT_SIMILARITY = "Doppelte Produktaehnlichkeit";
    public static final String DUPLICATE_PRODUCT_CATEGORY = "Doppelte Produktkategorie";
    public static final String DUPLICATE_BOOK_AUTHOR = "Doppelte Buchautor-Zuordnung";
    public static final String DUPLICATE_MUSIC_ARTIST = "Doppelte Musik-Kuenstler-Zuordnung";
    public static final String DUPLICATE_DVD_PARTICIPANT = "Doppelte DVD-Personen-Zuordnung";

    // Datentypen müssen eingehalten werden bzw. bedürfen einer Konvertierung
    public static final String VALUE_NOT_INTEGER = "Wert ist keine ganze Zahl";
    public static final String PRICE_NOT_VALID_NUMBER = "Preis ist keine gueltige Zahl";

    // NULL-Werte erkennen/beachten 
    public static final String ISBN_MISSING = "ISBN fehlt";
    public static final String REVIEWER_MISSING = "Rezensent fehlt";
    public static final String CATEGORY_NAME_MISSING = "Kategoriename fehlt";
    public static final String REQUIRED_FIELD_MISSING = "Pflichtfeld fehlt";

    // Fehler inhaltlicher, auch relationenübergreifender Art
    public static final String PRODUCT_NOT_FOUND = "Produkt existiert nicht";
    public static final String UNKNOWN_PRODUCT_TYPE = "Unbekannter Produkttyp";
    public static final String SIMILAR_PRODUCT_NOT_FOUND = "Ähnliches Produkt existiert nicht";
    public static final String SELF_SIMILAR_PRODUCT = "Produkt darf nicht sich selbst ähnlich sein";
    public static final String BOOK_AUTHOR_MISSING = "Buch hat keinen Autor";
    public static final String MUSIC_ARTIST_MISSING = "Musik-CD hat keinen Künstler";
    public static final String MUSIC_TRACK_MISSING = "Musik-CD hat keine Lieder";
    public static final String PRODUCT_CATEGORY_MISSING = "Produkt ist keiner Kategorie zugeordnet";

    public static String productTypeMismatch(String existingType) {
        return "Produkt wurde bereits mit anderem Typ geladen: " + existingType;
    }

    // Werte müssen sinnvoll sein, z.B. bzgl. des aktuellen Datums, Altersangaben, etc.
    public static final String RATING_OUT_OF_RANGE = "Bewertung muss zwischen 1 und 5 liegen";
    public static final String VALUE_MUST_BE_POSITIVE = "Wert muss groesser als 0 sein";
    public static final String VALUE_MUST_NOT_BE_NEGATIVE = "Wert darf nicht negativ sein";
    public static final String DATE_IN_FUTURE = "Datum darf nicht in der Zukunft liegen";
    public static final String PRICE_MUST_NOT_BE_NEGATIVE = "Preis darf nicht negativ sein";
}
