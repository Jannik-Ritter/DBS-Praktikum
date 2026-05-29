package de.dbspraktikum.loader.db.repositories;

import de.dbspraktikum.loader.db.Sql;
import de.dbspraktikum.loader.error.ErrorLog;
import de.dbspraktikum.loader.error.Errors;
import de.dbspraktikum.loader.parse.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public final class CategoryRepository {
    private final Connection connection;
    private final Map<String, Integer> categories = new HashMap<>();

    public CategoryRepository(Connection connection) {
        this.connection = connection;
    }

    public int categoryId(String name, String path, Integer parentId) throws SQLException {
        Integer existing = categories.get(path);
        if (existing != null) {
            return existing;
        }

        try (PreparedStatement statement = connection.prepareStatement(Sql.UPSERT_CATEGORY)) {
            statement.setString(1, name);
            statement.setString(2, path);
            JdbcUtil.setInteger(statement, 3, parentId); // Wenn Null dann Oberkategorie

            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                int id = rs.getInt(1);
                categories.put(path, id);
                return id;
            }
        }
    }

    public void insertProductCategory(String asin, int categoryId, String categoryPath, String source, ErrorLog errors) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(Sql.INSERT_PRODUCT_CATEGORY)) {
            statement.setString(1, asin);
            statement.setInt(2, categoryId);

            int changed = statement.executeUpdate();
            if (changed == 0) {
                errors.record("Produktkategorien", "Produktnummer/Kategorie", asin + "/" + categoryPath, source, Errors.DUPLICATE_PRODUCT_CATEGORY);
            }
        }
    }

    public void recordProductsWithoutCategory(String source, ErrorLog errors) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(Sql.SELECT_PRODUCTS_WITHOUT_CATEGORY);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                String asin = rs.getString(1);
                errors.record("Produktkategorien", "Kategorie", asin, source + ":" + asin, Errors.PRODUCT_CATEGORY_MISSING);
            }
        }
    }
}
