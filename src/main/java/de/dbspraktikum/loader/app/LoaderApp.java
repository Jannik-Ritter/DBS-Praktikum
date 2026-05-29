package de.dbspraktikum.loader.app;

import de.dbspraktikum.loader.db.Database;
import de.dbspraktikum.loader.db.repositories.ErrorRepository;
import de.dbspraktikum.loader.error.ErrorLog;
import de.dbspraktikum.loader.importer.CategoryXmlImporter;
import de.dbspraktikum.loader.importer.ReviewCsvImporter;
import de.dbspraktikum.loader.importer.ShopXmlImporter;

import java.sql.SQLException;
import java.util.Map;

public final class LoaderApp {
    private final LoaderConfig config;

    public LoaderApp(LoaderConfig config) {
        this.config = config;
    }

    public void run() throws Exception {
        try (Database database = Database.connect(config)) {
            if (config.schema() != null) {
                database.executeSchema(config.schema());
            }

            try (ErrorLog errors = new ErrorLog(config.rejects(), new ErrorRepository(database.connection()))) {
                ImportContext context = new ImportContext(database, errors);
                runImport(context);
                database.commit();
                printSummary(database, errors);
            } catch (Exception ex) {
                database.rollback();
                throw ex;
            }
        }
    }

    private void runImport(ImportContext context) throws Exception {
        // Produkt-Cache aus existierenden Produkten aufbauen
        context.products().loadExistingProducts();

        // Filialdaten importieren
        ShopXmlImporter shopImporter = new ShopXmlImporter(context);
        shopImporter.importFile(config.dataDir().resolve("dresden.xml"));
        shopImporter.importFile(config.dataDir().resolve("leipzig_transformed.xml"));
        // Ähnlichkeiten aufbauen
        context.products().insertSimilarRefs(context.similarRefs(), context.errors());

        // Kategorien importieren (nach dem Rest, weil die Verweise auf Produkte enthalten)
        new CategoryXmlImporter(context).importFile(config.dataDir().resolve("categories.xml"));
        context.categories().rejectProductsWithoutCategory("categories.xml", context.errors());
        context.products().loadExistingProducts();

        // Ratings importieren
        new ReviewCsvImporter(context).importFile(config.dataDir().resolve("reviews.csv"));
    }

    private static void printSummary(Database database, ErrorLog errors) throws SQLException {
        System.out.println("Import abgeschlossen.");
        System.out.printf("Produkte: %d%n", database.countProducts());
        System.out.printf("Rezensionen: %d%n", database.countReviews());
        System.out.printf("Kategorien: %d%n", database.countCategories());
        System.out.printf("Ladefehler: %d%n", database.countLoadErrors());

        var counts = errors.counts();
        if (counts.isEmpty()) {
            System.out.println("Keine Fehler gefunden.");
        } else {
            System.out.println("Fehler nach Art:");
            counts.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> System.out.printf("- %s: %d%n", e.getKey(), e.getValue()));
            System.out.printf("Insgesamt: %d%n", counts.values().stream().mapToInt(Integer::intValue).sum());
        }
    }
}
