package de.dbspraktikum.loader.db;

public final class Sql {
    private Sql() {
    }

    public static final String COUNT_PRODUCTS = countTable("Produkt");
    public static final String COUNT_REVIEWS = countTable("Kundenrezension");
    public static final String COUNT_CATEGORIES = countTable("Kategorie");
    public static final String COUNT_LOAD_ERRORS = countTable("Ladefehler");

    public static final String REFRESH_ALL_RATINGS = """
            UPDATE "Produkt" p
            SET "Rating" = COALESCE((
                SELECT round(avg(k."Punkte")::numeric, 2)
                FROM "Kundenrezension" k
                WHERE k."Produktnummer" = p."Produktnummer"
            ), 0.00)
            """;

    public static final String INSERT_LOAD_ERROR = """
            INSERT INTO "Ladefehler" ("Entity", "Attribut", "Rohwert", "Quelle", "Meldung")
            VALUES (?, ?, ?, ?, ?)
            """;

    public static final String SELECT_PRODUCT_TYPES = """
            SELECT "Produktnummer", "Produkttyp"
            FROM "Produkt"
            """;

    public static final String INSERT_PRODUCT = """
            INSERT INTO "Produkt" ("Produktnummer", "Titel", "Verkaufsrang", "Bild", "Detailseite", "EAN", "Produkttyp")
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    public static final String INSERT_BOOK = """
            INSERT INTO "Buch" (
                "Produktnummer", "Seitenzahl", "Erscheinungsdatum", "ISBN-Nummer",
                "Bindung", "Auflage", "Paketgewicht", "Pakethoehe", "Paketlaenge"
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT ("Produktnummer") DO NOTHING
            """;

    public static final String INSERT_MUSIC_CD = """
            INSERT INTO "Musik-CD" ("Produktnummer", "Erscheinungsdatum", "Bindung", "Format", "AnzahlDiscs", "UPC")
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT ("Produktnummer") DO NOTHING
            """;

    public static final String INSERT_DVD = """
            INSERT INTO "DVD" (
                "Produktnummer", "Format", "Laufzeit", "Region Code", "Erscheinungsdatum",
                "AspectRatio", "TheatricalRelease", "UPC"
            )
            VALUES (?, ?, CAST(? AS INTERVAL), ?, ?, ?, ?, ?)
            ON CONFLICT ("Produktnummer") DO NOTHING
            """;

    public static final String INSERT_TRACK = """
            INSERT INTO "Lied" ("Produktnummer", "Tracknummer", "Name")
            VALUES (?, ?, ?)
            ON CONFLICT DO NOTHING
            """;

    public static final String INSERT_PRODUCT_SIMILARITY = """
            INSERT INTO "Produktähnlichkeit" ("Produktnummer1", "Produktnummer2", "ÄhnlicherTitel")
            VALUES (?, ?, ?)
            ON CONFLICT ("Produktnummer1", "Produktnummer2") DO UPDATE
            SET "ÄhnlicherTitel" = COALESCE("Produktähnlichkeit"."ÄhnlicherTitel", EXCLUDED."ÄhnlicherTitel")
            """;

    public static final String INSERT_PRODUCT_AUDIO = """
            INSERT INTO "Produktaudio" ("Produktnummer", "Sprache", "Sprachtyp", "Audioformat")
            VALUES (?, ?, COALESCE(?, ''), COALESCE(?, ''))
            ON CONFLICT DO NOTHING
            """;

    public static final String UPSERT_OFFER = """
            INSERT INTO "Konditionen" ("FilialeID", "Produktnummer", "Zustand", "Preis", "Verfügbarkeit", "Währung")
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT ("FilialeID", "Produktnummer", "Zustand") DO UPDATE
            SET "Preis" = EXCLUDED."Preis",
                "Verfügbarkeit" = EXCLUDED."Verfügbarkeit",
                "Währung" = EXCLUDED."Währung"
            """;

    public static final String UPSERT_BRANCH = """
            INSERT INTO "Filiale" ("Name", "Anschrift", "Ort")
            VALUES (?, ?, ?)
            ON CONFLICT ("Name") DO UPDATE
            SET "Anschrift" = EXCLUDED."Anschrift",
                "Ort" = EXCLUDED."Ort"
            RETURNING "FilialeID"
            """;

    public static final String UPSERT_PUBLISHER = upsertByName("Verlag", "Verlagname", "VerlagID");
    public static final String UPSERT_LABEL = upsertByName("Label", "Labelname", "LabelID");
    public static final String UPSERT_STUDIO = upsertByName("Studio", "Studioname", "StudioID");
    public static final String UPSERT_LISTMANIA = upsertByName("ListmaniaListe", "Name", "ListmaniaID");
    public static final String UPSERT_PERSON = upsertByName("Person", "Name", "PersonID");
    public static final String UPSERT_CUSTOMER = upsertByName("Kunde", "Name", "KundeID");

    public static final String INSERT_ACTOR_ROLE = insertPersonRole("Actor");
    public static final String INSERT_AUTHOR_ROLE = insertPersonRole("Autor");
    public static final String INSERT_CREATOR_ROLE = insertPersonRole("Creator");
    public static final String INSERT_DIRECTOR_ROLE = insertPersonRole("Director");
    public static final String INSERT_ARTIST_ROLE = insertPersonRole("Künstler");

    public static final String INSERT_BOOK_AUTHOR = """
            INSERT INTO "Buchautoren" ("BuchID", "AutorID")
            VALUES (?, ?)
            ON CONFLICT DO NOTHING
            """;

    public static final String INSERT_BOOK_PUBLISHER = """
            INSERT INTO "Buchverlage" ("BuchID", "VerlagID")
            SELECT ?, ?
            WHERE EXISTS (
                SELECT 1 FROM "Buch" WHERE "Produktnummer" = ?
            )
            ON CONFLICT DO NOTHING
            """;

    public static final String INSERT_MUSIC_ARTIST = """
            INSERT INTO "Beteiligte Künstler" ("Produktnummer", "KünstlerID")
            VALUES (?, ?)
            ON CONFLICT DO NOTHING
            """;

    public static final String INSERT_MUSIC_CD_LABEL = """
            INSERT INTO "MusikCDLabels" ("Produktnummer", "LabelID")
            SELECT ?, ?
            WHERE EXISTS (
                SELECT 1 FROM "Musik-CD" WHERE "Produktnummer" = ?
            )
            ON CONFLICT DO NOTHING
            """;

    public static final String INSERT_PRODUCT_STUDIO = """
            INSERT INTO "Produktstudios" ("Produktnummer", "StudioID")
            VALUES (?, ?)
            ON CONFLICT DO NOTHING
            """;

    public static final String INSERT_PRODUCT_LISTMANIA = """
            INSERT INTO "ProduktListmania" ("Produktnummer", "ListmaniaID")
            VALUES (?, ?)
            ON CONFLICT DO NOTHING
            """;

    public static final String INSERT_DVD_PARTICIPANT = """
            INSERT INTO "Beteiligte Personen" ("Produktnummer", "PersonID", "Rolle")
            VALUES (?, ?, ?)
            ON CONFLICT DO NOTHING
            """;

    public static final String UPSERT_CATEGORY = """
            INSERT INTO "Kategorie" ("Name", "Pfad", "OberkategorieID")
            VALUES (?, ?, ?)
            ON CONFLICT ("Pfad") DO UPDATE SET "Name" = EXCLUDED."Name"
            RETURNING "KategorieID"
            """;

    public static final String INSERT_PRODUCT_CATEGORY = """
            INSERT INTO "Produktkategorien" ("Produktnummer", "KategorieID")
            VALUES (?, ?)
            ON CONFLICT DO NOTHING
            """;

    public static final String SELECT_PRODUCTS_WITHOUT_CATEGORY = """
            SELECT p."Produktnummer"
            FROM "Produkt" p
            WHERE NOT EXISTS (
                SELECT 1
                FROM "Produktkategorien" pk
                WHERE pk."Produktnummer" = p."Produktnummer"
            )
            ORDER BY p."Produktnummer"
            """;

    public static final String DELETE_PRODUCT_BY_NUMBER = """
            DELETE FROM "Produkt"
            WHERE "Produktnummer" = ?
            """;

    public static final String INSERT_REVIEW = """
            INSERT INTO "Kundenrezension" ("KundeID", "Produktnummer", "Rezensionsdatum", "Helpful", "Summary", "Text", "Punkte")
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT ("KundeID", "Produktnummer") DO NOTHING
            """;

    private static String countTable(String table) {
        return "SELECT count(*) FROM \"" + table + "\"";
    }

    private static String upsertByName(String table, String nameColumn, String idColumn) {
        return """
                WITH input AS (
                    SELECT ?::varchar AS "%2$s"
                ),
                existing AS (
                    SELECT t."%3$s"
                    FROM "%1$s" t
                    JOIN input i ON t."%2$s" = i."%2$s"
                    LIMIT 1
                ),
                inserted AS (
                    INSERT INTO "%1$s" ("%2$s")
                    SELECT "%2$s"
                    FROM input
                    WHERE NOT EXISTS (SELECT 1 FROM existing)
                    RETURNING "%3$s"
                )
                SELECT "%3$s" FROM existing
                UNION ALL
                SELECT "%3$s" FROM inserted
                LIMIT 1
                """.formatted(table, nameColumn, idColumn);
    }

    private static String insertPersonRole(String roleTable) {
        return """
                INSERT INTO "%s" ("PersonID")
                VALUES (?)
                ON CONFLICT DO NOTHING
                """.formatted(roleTable);
    }
}
