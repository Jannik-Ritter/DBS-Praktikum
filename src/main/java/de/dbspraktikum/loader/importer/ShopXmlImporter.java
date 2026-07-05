package de.dbspraktikum.loader.importer;

import de.dbspraktikum.loader.app.ImportContext;
import de.dbspraktikum.loader.error.Errors;
import de.dbspraktikum.loader.model.SimilarRef;
import de.dbspraktikum.loader.parse.TextUtil;
import de.dbspraktikum.loader.parse.XmlUtil;
import de.dbspraktikum.loader.validation.Validation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.time.LocalDate;
import java.util.List;

public final class ShopXmlImporter extends Importer {

    public ShopXmlImporter(ImportContext context) {
        super(context);
    }

    @Override
    public void importFile(Path path) throws Exception {
        if (!requireFile(path)) {
            return;
        }

        Document document = XmlUtil.parseXml(path);
        Element shop = document.getDocumentElement();
        int branchId = context.references().upsertBranch(
                TextUtil.clean(shop.getAttribute("name")),
                TextUtil.clean(shop.getAttribute("street")),
                TextUtil.clean(shop.getAttribute("zip"))
        );

        for (Element item : XmlUtil.children(shop, "item")) {
            loadItem(path.getFileName().toString(), branchId, item);
        }
    }

    private void loadItem(String source, int branchId, Element item) throws SQLException {
        String asin = Validation.normalizeAsin(item.getAttribute("asin"));
        String rawType = TextUtil.clean(item.getAttribute("pgroup"));
        String type = Validation.mapProductType(rawType);

        if (!Validation.validAsin(asin)) {
            context.errors().record("Produkt", "Produktnummer", item.getAttribute("asin"), source, Errors.INVALID_PRODUCT_NUMBER);
            return;
        }
        if (type == null) {
            context.errors().record("Produkt", "Produkttyp", rawType, source + ":" + asin, Errors.UNKNOWN_PRODUCT_TYPE);
            return;
        }

        String existingType = context.products().typeOf(asin);
        if (existingType != null) {
            if (!existingType.equals(type)) {
                context.errors().record("Produkt", "Produkttyp", type, source + ":" + asin, Errors.productTypeMismatch(existingType));
                return;
            }
            loadOffers(source, branchId, asin, item);
            loadSubtypeReferences(type, asin, item, source, false);
            loadSupplementalData(asin, item);
            collectSimilarRefs(source, asin, item);
            return;
        }

        String bookIsbn = null;
        if ("Buch".equals(type)) {
            bookIsbn = validBookIsbn(source, asin, item);
            if (bookIsbn == null) {
                return;
            }
        }

        String title = TextUtil.clean(XmlUtil.firstText(item, "title"));
        if (title == null || title.isBlank()) {
            context.errors().record("Produkt", "Titel", null, source + ":" + asin, Errors.REQUIRED_FIELD_MISSING);
            return;
        }

        Integer salesRank = context.parser().positiveInt(item.getAttribute("salesrank"), "Produkt", "Verkaufsrang", source + ":" + asin);
        // Werte stehen je nach Datei als Attribut am Item oder in Unterelementen
        String picture = TextUtil.firstNonBlank(item.getAttribute("picture"), XmlUtil.attr(XmlUtil.firstChild(item, "details"), "img"));
        String detailPage = TextUtil.firstNonBlank(item.getAttribute("detailpage"), XmlUtil.firstText(item, "details"));
        String ean = TextUtil.firstNonBlank(item.getAttribute("ean"), XmlUtil.firstText(item, "ean"));
        if (ean != null && !Validation.validEan13(ean)) {
            context.errors().record("Produkt", "EAN", ean, source + ":" + asin, Errors.EAN_INVALID_FORMAT);
            ean = null;
        }

        Savepoint savepoint = context.database().connection().setSavepoint();
        boolean subtypeInserted;
        try {
            if (!context.products().insertProduct(asin, title, type, salesRank, picture, detailPage, ean, source, context.errors())) {
                context.database().connection().releaseSavepoint(savepoint);
                return;
            }

            subtypeInserted = switch (type) {
                case "Buch" -> loadBookBase(source, asin, item, bookIsbn);
                case "Musik-CD" -> loadMusicBase(source, asin, item);
                case "DVD" -> loadDvdBase(source, asin, item);
                default -> throw new IllegalStateException("Nicht behandelter Produkttyp: " + type);
            };
            if (!subtypeInserted) {
                context.database().connection().rollback(savepoint);
                context.products().forgetProduct(asin);
                return;
            }
            context.database().connection().releaseSavepoint(savepoint);
        } catch (SQLException ex) {
            context.database().connection().rollback(savepoint);
            context.products().forgetProduct(asin);
            throw ex;
        }

        loadSubtypeReferences(type, asin, item, source, true);
        loadOffers(source, branchId, asin, item);
        loadSupplementalData(asin, item);
        // Ähnliche Produkte werden gesammelt und erst nach allen Shop-Dateien gespeichert
        collectSimilarRefs(source, asin, item);
    }

    private String validBookIsbn(String source, String asin, Element item) {
        Element spec = XmlUtil.firstChild(item, "bookspec");
        String rawIsbn = TextUtil.clean(XmlUtil.attr(XmlUtil.firstChild(spec, "isbn"), "val"));
        if (rawIsbn == null) {
            context.errors().record("Buch", "ISBN-Nummer", null, source + ":" + asin, Errors.ISBN_MISSING);
            return null;
        } else if (!Validation.validIsbn10(rawIsbn)) {
            context.errors().record("Buch", "ISBN-Nummer", rawIsbn, source + ":" + asin, Errors.ISBN_INVALID_FORMAT);
            return null;
        }
        return rawIsbn;
    }

    private boolean loadBookBase(String source, String asin, Element item, String rawIsbn) throws SQLException {
        Element spec = XmlUtil.firstChild(item, "bookspec");
        Integer pages = context.parser().positiveInt(XmlUtil.text(XmlUtil.firstChild(spec, "pages")), "Buch", "Seitenzahl", source + ":" + asin);
        LocalDate publication = context.parser().date(XmlUtil.attr(XmlUtil.firstChild(spec, "publication"), "date"), "Buch", "Erscheinungsdatum", source + ":" + asin);
        String binding = TextUtil.clean(XmlUtil.firstText(spec, "binding"));
        String edition = TextUtil.clean(XmlUtil.attr(XmlUtil.firstChild(spec, "edition"), "val"));
        Element packageElement = XmlUtil.firstChild(spec, "package");
        BigDecimal packageWeight = context.parser().decimal(XmlUtil.attr(packageElement, "weight"), "Buch", "Paketgewicht", source + ":" + asin);
        BigDecimal packageHeight = context.parser().decimal(XmlUtil.attr(packageElement, "height"), "Buch", "Pakethoehe", source + ":" + asin);
        BigDecimal packageLength = context.parser().decimal(XmlUtil.attr(packageElement, "length"), "Buch", "Paketlaenge", source + ":" + asin);

        return context.products().insertBook(
                asin, pages, publication, rawIsbn,
                binding, edition, packageWeight, packageHeight, packageLength,
                source, context.errors());
    }

    private void loadBookReferences(String source, String asin, Element item, boolean reportDuplicates) throws SQLException {
        loadBookPublishers(asin, item);

        List<String> authors = XmlUtil.valuesFromContainer(item, "authors", "author");
        if (authors.isEmpty()) {
            // Manche Dateien liefern Autoren über creators statt authors
            authors = XmlUtil.valuesFromContainer(item, "creators", "creator");
        }
        if (authors.isEmpty()) {
            context.errors().record("Buchautoren", "Autor", asin, source + ":" + asin, Errors.BOOK_AUTHOR_MISSING);
        }
        for (String author : authors) {
            int personId = context.references().personId(author);
            context.references().insertRole(personId, "Autor");
            context.references().insertBookAuthor(asin, personId, author, source, context.errors(), reportDuplicates);
        }
    }

    private boolean loadMusicBase(String source, String asin, Element item) throws SQLException {
        Element spec = XmlUtil.firstChild(item, "musicspec");
        LocalDate releaseDate = context.parser().date(
                TextUtil.firstNonBlank(XmlUtil.firstText(spec, "releasedate"), XmlUtil.attr(XmlUtil.firstChild(spec, "releasedate"), "date")),
                "Musik-CD", "Erscheinungsdatum", source + ":" + asin);
        String binding = TextUtil.clean(XmlUtil.firstText(spec, "binding"));
        String format = TextUtil.clean(TextUtil.firstNonBlank(XmlUtil.firstText(spec, "format"), XmlUtil.attr(XmlUtil.firstChild(spec, "format"), "value")));
        Integer discCount = context.parser().positiveInt(XmlUtil.firstText(spec, "num_discs"), "Musik-CD", "AnzahlDiscs", source + ":" + asin);
        String upc = TextUtil.clean(TextUtil.firstNonBlank(XmlUtil.firstText(spec, "upc"), XmlUtil.attr(XmlUtil.firstChild(spec, "upc"), "val")));

        return context.products().insertMusicCd(asin, releaseDate, binding, format, discCount, upc, source, context.errors());
    }

    private void loadMusicReferences(String source, String asin, Element item, boolean reportDuplicates) throws SQLException {
        loadMusicLabels(asin, item);

        List<String> artists = XmlUtil.valuesFromContainer(item, "artists", "artist");
        if (artists.isEmpty()) {
            // Manche Dateien liefern Künstler über creators statt artists
            artists = XmlUtil.valuesFromContainer(item, "creators", "creator");
        }
        if (artists.isEmpty()) {
            context.errors().record("Beteiligte Künstler", "Künstler", asin, source + ":" + asin, Errors.MUSIC_ARTIST_MISSING);
        }
        for (String artist : artists) {
            int personId = context.references().personId(artist);
            context.references().insertRole(personId, "Künstler");
            context.references().insertMusicArtist(asin, personId, artist, source, context.errors(), reportDuplicates);
        }
        List<String> tracks = XmlUtil.valuesFromContainer(item, "tracks", "title");
        if (tracks.isEmpty()) {
            context.errors().record("Lied", "Name", asin, source + ":" + asin, Errors.MUSIC_TRACK_MISSING);
        }
        int trackNumber = 1;
        for (String track : tracks) {
            String name = TextUtil.clean(track);
            if (name != null) {
                context.products().insertTrack(asin, trackNumber, name, source, context.errors(), reportDuplicates);
                trackNumber++;
            }
        }
    }

    private boolean loadDvdBase(String source, String asin, Element item) throws SQLException {
        Element spec = XmlUtil.firstChild(item, "dvdspec");
        String format = TextUtil.clean(TextUtil.firstNonBlank(XmlUtil.firstText(spec, "format"), XmlUtil.attr(XmlUtil.firstChild(spec, "format"), "value")));
        Integer runtime = context.parser().positiveInt(XmlUtil.firstText(spec, "runningtime"), "DVD", "Laufzeit", source + ":" + asin);
        Integer regionCode = context.parser().nonNegativeInt(XmlUtil.firstText(spec, "regioncode"), "DVD", "Region Code", source + ":" + asin);
        LocalDate releaseDate = context.parser().date(XmlUtil.firstText(spec, "releasedate"), "DVD", "Erscheinungsdatum", source + ":" + asin);
        String aspectRatio = TextUtil.clean(XmlUtil.firstText(spec, "aspectratio"));
        String theatricalRelease = TextUtil.clean(XmlUtil.firstText(spec, "theatr_release"));
        String upc = TextUtil.clean(TextUtil.firstNonBlank(XmlUtil.firstText(spec, "upc"), XmlUtil.attr(XmlUtil.firstChild(spec, "upc"), "val")));

        return context.products().insertDvd(asin, format, runtime, regionCode, releaseDate, aspectRatio, theatricalRelease, upc, source, context.errors());
    }

    private void loadDvdReferences(String source, String asin, Element item, boolean reportDuplicates) throws SQLException {
        List<String> actors = XmlUtil.valuesFromContainer(item, "actors", "actor");
        List<String> creators = XmlUtil.valuesFromContainer(item, "creators", "creator");
        List<String> directors = XmlUtil.valuesFromContainer(item, "directors", "director");
        insertPeopleByRole(source, actors, "Actor", asin, reportDuplicates);
        insertPeopleByRole(source, creators, "Creator", asin, reportDuplicates);
        insertPeopleByRole(source, directors, "Director", asin, reportDuplicates);
    }

    private void insertPeopleByRole(String source, List<String> names, String role, String productNumber, boolean reportDuplicates) throws SQLException {
        for (String name : names) {
            int personId = context.references().personId(name);
            context.references().insertRole(personId, role);
            context.references().insertDvdParticipant(productNumber, personId, role, name, source, context.errors(), reportDuplicates);
        }
    }

    private void loadSubtypeReferences(String type, String asin, Element item, String source, boolean reportDuplicates) throws SQLException {
        switch (type) {
            case "Buch" -> loadBookReferences(source, asin, item, reportDuplicates);
            case "Musik-CD" -> loadMusicReferences(source, asin, item, reportDuplicates);
            case "DVD" -> loadDvdReferences(source, asin, item, reportDuplicates);
            default -> throw new IllegalStateException("Nicht behandelter Produkttyp: " + type);
        }
    }

    private void loadBookPublishers(String asin, Element item) throws SQLException {
        for (String publisher : XmlUtil.valuesFromContainer(item, "publishers", "publisher")) {
            int publisherId = context.references().publisherId(publisher);
            context.references().insertBookPublisher(asin, publisherId);
        }
    }

    private void loadMusicLabels(String asin, Element item) throws SQLException {
        for (String label : XmlUtil.valuesFromContainer(item, "labels", "label")) {
            int labelId = context.references().labelId(label);
            context.references().insertMusicCdLabel(asin, labelId);
        }
    }

    private void loadOffers(String source, int branchId, String asin, Element item) throws SQLException {
        List<Element> prices = XmlUtil.children(item, "price");
        if (prices.isEmpty()) {
            loadOffer(source, branchId, asin, item, null);
            return;
        }

        for (Element price : prices) {
            loadOffer(source, branchId, asin, item, price);
        }
    }

    private void loadOffer(String source, int branchId, String asin, Element item, Element price) throws SQLException {
        String state = TextUtil.firstNonBlank(XmlUtil.attr(price, "state"), XmlUtil.attr(item, "state"), "unknown");

        String currency = TextUtil.clean(XmlUtil.attr(price, "currency"));
        if (currency != null && !Validation.validCurrencyCode(currency)) {
            context.errors().record("Konditionen", "Währung", currency, source + ":" + asin, Errors.CURRENCY_INVALID_FORMAT);
            currency = null;
        }

        BigDecimal priceValue = context.parser().price(XmlUtil.text(price), XmlUtil.attr(price, "mult"), source + ":" + asin);
        context.offers().upsertOffer(branchId, asin, state, priceValue, currency);
    }

    private void loadSupplementalData(String asin, Element item) throws SQLException {
        for (String studio : XmlUtil.valuesFromContainer(item, "studios", "studio")) {
            int studioId = context.references().studioId(studio);
            context.references().insertProductStudio(asin, studioId);
        }

        for (String list : XmlUtil.valuesFromContainer(item, "listmania", "list")) {
            int listmaniaId = context.references().listmaniaId(list);
            context.references().insertProductListmania(asin, listmaniaId);
        }

        Element audioText = XmlUtil.firstChild(item, "audiotext");
        String audioFormat = TextUtil.clean(XmlUtil.firstText(audioText, "audioformat"));
        for (Element language : XmlUtil.children(audioText, "language")) {
            String value = TextUtil.clean(XmlUtil.text(language));
            if (value != null) {
                context.products().insertAudio(asin, value, TextUtil.clean(language.getAttribute("type")), audioFormat);
            }
        }
    }

    private void collectSimilarRefs(String source, String asin, Element item) {
        Element similars = XmlUtil.firstChild(item, "similars");
        if (similars == null) {
            return;
        }

        for (Element similar : XmlUtil.children(similars)) {
            String ref = Validation.normalizeAsin(TextUtil.firstNonBlank(similar.getAttribute("asin"), XmlUtil.firstText(similar, "asin")));
            String title = TextUtil.clean(TextUtil.firstNonBlank(similar.getAttribute("title"), XmlUtil.firstText(similar, "title"), XmlUtil.directText(similar)));
            if (ref != null) {
                context.similarRefs().add(new SimilarRef(asin, ref, title, source));
            }
        }
    }
}
