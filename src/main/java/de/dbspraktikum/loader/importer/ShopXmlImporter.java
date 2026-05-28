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
            context.errors().record("Produkt", "Produktnummer", item.getAttribute("asin"), source, Errors.ASIN_MISSING_OR_INVALID);
            return;
        }
        if (type == null) {
            context.errors().record("Produkt", "Produkttyp", rawType, source + ":" + asin, Errors.UNKNOWN_PRODUCT_TYPE);
            return;
        }

        String title = TextUtil.clean(XmlUtil.firstText(item, "title"));
        if (title == null || title.isBlank()) {
            context.errors().record("Produkt", "Titel", null, source + ":" + asin, Errors.REQUIRED_FIELD_MISSING);
            return;
        }

        Integer salesRank = context.parser().positiveInt(item.getAttribute("salesrank"), "Produkt", "Verkaufsrang", source + ":" + asin);
        String picture = TextUtil.firstNonBlank(item.getAttribute("picture"), XmlUtil.attr(XmlUtil.firstChild(item, "details"), "img"));
        String detailPage = TextUtil.firstNonBlank(item.getAttribute("detailpage"), XmlUtil.firstText(item, "details"));
        String ean = TextUtil.firstNonBlank(item.getAttribute("ean"), XmlUtil.firstText(item, "ean"));

        if (!context.products().insertProduct(asin, title, type, salesRank, picture, detailPage, ean, source, context.errors())) {
            return;
        }

        switch (type) {
            case "Buch" -> loadBook(source, asin, item);
            case "Musik-CD" -> loadMusic(source, asin, item);
            case "DVD" -> loadDvd(source, asin, item);
            default -> throw new IllegalStateException("Nicht behandelter Produkttyp: " + type);
        }

        loadOffer(source, branchId, asin, item);
        collectSimilarRefs(source, asin, item);
    }

    private void loadBook(String source, String asin, Element item) throws SQLException {
        Element spec = XmlUtil.firstChild(item, "bookspec");
        String isbn = TextUtil.firstNonBlank(XmlUtil.attr(XmlUtil.firstChild(spec, "isbn"), "val"), asin);
        Integer pages = context.parser().positiveInt(XmlUtil.text(XmlUtil.firstChild(spec, "pages")), "Buch", "Seitenzahl", source + ":" + asin);
        LocalDate publication = context.parser().date(XmlUtil.attr(XmlUtil.firstChild(spec, "publication"), "date"), "Buch", "Erscheinungsdatum", source + ":" + asin);
        Integer publisherId = firstIdFromContainer(item, "publishers", "publisher", "Verlag");

        context.products().insertBook(asin, pages, publication, isbn, publisherId);

        for (String author : XmlUtil.valuesFromContainer(item, "authors", "author")) {
            int personId = context.references().personId(author);
            context.references().insertRole(personId, "Autor");
            context.references().insertBookAuthor(asin, personId);
        }
    }

    private void loadMusic(String source, String asin, Element item) throws SQLException {
        Element spec = XmlUtil.firstChild(item, "musicspec");
        LocalDate releaseDate = context.parser().date(
                TextUtil.firstNonBlank(XmlUtil.firstText(spec, "releasedate"), XmlUtil.attr(XmlUtil.firstChild(spec, "releasedate"), "date")),
                "Musik-CD", "Erscheinungsdatum", source + ":" + asin);
        Integer labelId = firstIdFromContainer(item, "labels", "label", "Label");

        context.products().insertMusicCd(asin, labelId, releaseDate);

        for (String artist : XmlUtil.valuesFromContainer(item, "artists", "artist")) {
            int personId = context.references().personId(artist);
            context.references().insertRole(personId, "Künstler");
            context.references().insertMusicArtist(asin, personId);
        }
        for (String track : XmlUtil.valuesFromContainer(item, "tracks", "title")) {
            String name = TextUtil.clean(track);
            if (name != null) {
                context.products().insertTrack(asin, name);
            }
        }
    }

    private void loadDvd(String source, String asin, Element item) throws SQLException {
        Element spec = XmlUtil.firstChild(item, "dvdspec");
        String format = TextUtil.clean(TextUtil.firstNonBlank(XmlUtil.firstText(spec, "format"), XmlUtil.attr(XmlUtil.firstChild(spec, "format"), "value")));
        Integer runtime = context.parser().positiveInt(XmlUtil.firstText(spec, "runningtime"), "DVD", "Laufzeit", source + ":" + asin);
        Integer regionCode = context.parser().nonNegativeInt(XmlUtil.firstText(spec, "regioncode"), "DVD", "Region Code", source + ":" + asin);
        LocalDate releaseDate = context.parser().date(XmlUtil.firstText(spec, "releasedate"), "DVD", "Erscheinungsdatum", source + ":" + asin);

        context.products().insertDvd(asin, format, runtime, regionCode, releaseDate);
        insertPeopleByRole(item, "actors", "actor", "Actor", asin);
        insertPeopleByRole(item, "creators", "creator", "Creator", asin);
        insertPeopleByRole(item, "directors", "director", "Director", asin);
    }

    private void insertPeopleByRole(Element item, String container, String child, String role, String productNumber) throws SQLException {
        for (String name : XmlUtil.valuesFromContainer(item, container, child)) {
            int personId = context.references().personId(name);
            context.references().insertRole(personId, role);
            context.references().insertDvdParticipant(productNumber, personId, role);
        }
    }

    private Integer firstIdFromContainer(Element item, String container, String child, String target) throws SQLException {
        List<String> values = XmlUtil.valuesFromContainer(item, container, child);
        if (values.isEmpty()) {
            return null;
        }
        if ("Verlag".equals(target)) {
            return context.references().publisherId(values.getFirst());
        }
        if ("Label".equals(target)) {
            return context.references().labelId(values.getFirst());
        }
        throw new IllegalArgumentException("Unbekanntes Ziel: " + target);
    }

    private void loadOffer(String source, int branchId, String asin, Element item) throws SQLException {
        Element price = XmlUtil.firstChild(item, "price");
        String state = TextUtil.firstNonBlank(XmlUtil.attr(price, "state"), XmlUtil.attr(item, "state"), "unknown");
        String currency = TextUtil.clean(XmlUtil.attr(price, "currency"));
        BigDecimal priceValue = context.parser().price(XmlUtil.text(price), XmlUtil.attr(price, "mult"), source + ":" + asin);
        context.offers().upsertOffer(branchId, asin, state, priceValue, currency);
    }

    private void collectSimilarRefs(String source, String asin, Element item) {
        Element similars = XmlUtil.firstChild(item, "similars");
        if (similars == null) {
            return;
        }

        for (Element similar : XmlUtil.children(similars)) {
            String ref = Validation.normalizeAsin(TextUtil.firstNonBlank(similar.getAttribute("asin"), XmlUtil.firstText(similar, "asin")));
            if (ref != null) {
                context.similarRefs().add(new SimilarRef(asin, ref, source));
            }
        }
    }
}
