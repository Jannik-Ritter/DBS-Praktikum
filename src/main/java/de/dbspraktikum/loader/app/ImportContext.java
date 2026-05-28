package de.dbspraktikum.loader.app;

import de.dbspraktikum.loader.db.Database;
import de.dbspraktikum.loader.db.repositories.CategoryRepository;
import de.dbspraktikum.loader.db.repositories.OfferRepository;
import de.dbspraktikum.loader.db.repositories.ProductRepository;
import de.dbspraktikum.loader.db.repositories.ReferenceRepository;
import de.dbspraktikum.loader.db.repositories.ReviewRepository;
import de.dbspraktikum.loader.error.ErrorLog;
import de.dbspraktikum.loader.model.SimilarRef;
import de.dbspraktikum.loader.parse.ValueParser;

import java.util.ArrayList;
import java.util.List;

public final class ImportContext {
    private final Database database;
    private final ErrorLog errors;
    private final ValueParser parser;
    private final ProductRepository products;
    private final ReferenceRepository references;
    private final OfferRepository offers;
    private final CategoryRepository categories;
    private final ReviewRepository reviews;
    private final List<SimilarRef> similarRefs = new ArrayList<>();

    public ImportContext(Database database, ErrorLog errors) {
        this.database = database;
        this.errors = errors;
        this.parser = new ValueParser(errors);
        this.products = new ProductRepository(database.connection());
        this.references = new ReferenceRepository(database.connection());
        this.offers = new OfferRepository(database.connection());
        this.categories = new CategoryRepository(database.connection());
        this.reviews = new ReviewRepository(database.connection());
    }

    public Database database() {
        return database;
    }

    public ErrorLog errors() {
        return errors;
    }

    public ValueParser parser() {
        return parser;
    }

    public ProductRepository products() {
        return products;
    }

    public ReferenceRepository references() {
        return references;
    }

    public OfferRepository offers() {
        return offers;
    }

    public CategoryRepository categories() {
        return categories;
    }

    public ReviewRepository reviews() {
        return reviews;
    }

    public List<SimilarRef> similarRefs() {
        return similarRefs;
    }
}
