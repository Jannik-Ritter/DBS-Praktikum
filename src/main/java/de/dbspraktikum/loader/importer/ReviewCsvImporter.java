package de.dbspraktikum.loader.importer;

import de.dbspraktikum.loader.app.ImportContext;
import de.dbspraktikum.loader.parse.CsvReader;
import de.dbspraktikum.loader.parse.TextUtil;
import de.dbspraktikum.loader.validation.Validation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;

public final class ReviewCsvImporter {
    private final ImportContext context;

    public ReviewCsvImporter(ImportContext context) {
        this.context = context;
    }

    public void importFile(Path path) throws Exception {
        if (!Files.exists(path)) {
            context.errors().record("Datei", "Pfad", path.toString(), path.toString(), "Datei nicht gefunden");
            return;
        }

        try (CsvReader reader = new CsvReader(path)) {
            CsvReader.Row row;
            while ((row = reader.next()) != null) {
                loadReview(path.getFileName().toString() + ":" + row.lineNumber(), row);
            }
        }
    }

    private void loadReview(String source, CsvReader.Row row) throws SQLException {
        String asin = Validation.normalizeAsin(row.value("product"));
        if (!Validation.validAsin(asin)) {
            context.errors().record("Kundenrezension", "Produktnummer", row.value("product"), source, "Ungueltige ASIN");
            return;
        }
        if (!context.products().exists(asin)) {
            context.errors().record("Kundenrezension", "Produktnummer", asin, source, "Produkt existiert nicht");
            return;
        }

        Integer rating = context.parser().integer(row.value("rating"), "Kundenrezension", "Punkte", source);
        if (rating == null || rating < 1 || rating > 5) {
            context.errors().record("Kundenrezension", "Punkte", row.value("rating"), source, "Bewertung muss zwischen 1 und 5 liegen");
            return;
        }

        String user = TextUtil.clean(row.value("user"));
        if (user == null) {
            context.errors().record("Kundenrezension", "user", null, source, "Rezensent fehlt");
            return;
        }

        LocalDate date = context.parser().date(row.value("reviewdate"), "Kundenrezension", "Rezensionsdatum", source);
        Integer helpful = context.parser().nonNegativeInt(row.value("helpful"), "Kundenrezension", "Helpful", source);
        String summary = TextUtil.clean(row.value("summary"));
        String content = TextUtil.firstNonBlank(row.value("content"), summary, "");

        int customerId = context.references().customerId(user);
        context.reviews().insertReview(customerId, asin, date, helpful, summary, content, rating, user, source, context.errors());
    }
}
