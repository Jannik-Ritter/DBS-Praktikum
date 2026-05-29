package de.dbspraktikum.loader.importer;

import de.dbspraktikum.loader.app.ImportContext;
import de.dbspraktikum.loader.error.Errors;
import de.dbspraktikum.loader.parse.CsvReader;
import de.dbspraktikum.loader.parse.TextUtil;
import de.dbspraktikum.loader.validation.Validation;

import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;

public final class ReviewCsvImporter extends Importer {

    public ReviewCsvImporter(ImportContext context) {
        super(context);
    }

    @Override
    public void importFile(Path path) throws Exception {
        if (!requireFile(path)) {
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
            context.errors().record("Kundenrezension", "Produktnummer", row.value("product"), source, Errors.INVALID_PRODUCT_NUMBER);
            return;
        }
        if (!context.products().exists(asin)) {
            context.errors().record("Kundenrezension", "Produktnummer", asin, source, Errors.PRODUCT_NOT_FOUND);
            return;
        }

        Integer rating = context.parser().integer(row.value("rating"), "Kundenrezension", "Punkte", source);
        if (rating == null) {
            return;
        }
        if (rating < 1 || rating > 5) {
            context.errors().record("Kundenrezension", "Punkte", row.value("rating"), source, Errors.RATING_OUT_OF_RANGE);
            return;
        }

        String user = TextUtil.clean(row.value("user"));
        if (user == null) {
            context.errors().record("Kundenrezension", "user", null, source, Errors.REVIEWER_MISSING);
            return;
        }

        LocalDate date = context.parser().date(row.value("reviewdate"), "Kundenrezension", "Rezensionsdatum", source);
        Integer helpful = context.parser().nonNegativeInt(row.value("helpful"), "Kundenrezension", "Helpful", source);
        String summary = TextUtil.clean(row.value("summary"));
        String content = TextUtil.firstNonBlank(row.value("content"), summary, "");

        // Anonyme Gäste werden über den Quellbezeichner pro Zeile eindeutig gemacht
        String customerName = "guest".equalsIgnoreCase(user) ? user + "@" + source : user;
        int customerId = context.references().customerId(customerName);
        context.reviews().insertReview(customerId, asin, date, helpful, summary, content, rating, user, source, context.errors());
    }
}
