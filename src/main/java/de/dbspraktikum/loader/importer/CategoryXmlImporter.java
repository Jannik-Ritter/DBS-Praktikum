package de.dbspraktikum.loader.importer;

import de.dbspraktikum.loader.app.ImportContext;
import de.dbspraktikum.loader.error.Errors;
import de.dbspraktikum.loader.parse.TextUtil;
import de.dbspraktikum.loader.parse.XmlUtil;
import de.dbspraktikum.loader.validation.Validation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.nio.file.Path;
import java.sql.SQLException;

public final class CategoryXmlImporter extends Importer {

    public CategoryXmlImporter(ImportContext context) {
        super(context);
    }

    @Override
    public void importFile(Path path) throws Exception {
        if (!requireFile(path)) {
            return;
        }

        Document document = XmlUtil.parseXml(path);
        Element root = document.getDocumentElement();
        for (Element category : XmlUtil.children(root, "category")) {
            loadCategory(path.getFileName().toString(), category, null, null);
        }
    }

    private void loadCategory(String source, Element element, Integer parentId, String parentPath) throws SQLException {
        String name = TextUtil.clean(XmlUtil.directText(element));
        if (name == null) {
            context.errors().record("Kategorie", "Name", null, source, Errors.CATEGORY_NAME_MISSING);
            return;
        }

        // Der hierarchische Pfad macht Kategorien trotz gleicher Namen eindeutig
        String path = parentPath == null ? name : parentPath + "/" + name;
        int categoryId = context.categories().categoryId(name, path, parentId);

        for (Element item : XmlUtil.children(element, "item")) {
            String asin = Validation.normalizeAsin(XmlUtil.text(item));
            if (!Validation.validAsin(asin)) {
                context.errors().record("Produktkategorien", "Produktnummer", XmlUtil.text(item), source + ":" + path, Errors.INVALID_PRODUCT_NUMBER);
                continue;
            }
            if (!context.products().exists(asin)) {
                context.errors().record("Produktkategorien", "Produktnummer", asin, source + ":" + path, Errors.PRODUCT_NOT_FOUND);
                continue;
            }
            context.categories().insertProductCategory(asin, categoryId, path, source + ":" + path, context.errors());
        }

        for (Element child : XmlUtil.children(element, "category")) {
            loadCategory(source, child, categoryId, path);
        }
    }
}
