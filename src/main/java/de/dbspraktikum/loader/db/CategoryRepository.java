package de.dbspraktikum.loader.db;

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
            JdbcUtil.setInteger(statement, 3, parentId);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                int id = rs.getInt(1);
                categories.put(path, id);
                return id;
            }
        }
    }

    public void insertProductCategory(String asin, int categoryId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(Sql.INSERT_PRODUCT_CATEGORY)) {
            statement.setString(1, asin);
            statement.setInt(2, categoryId);
            statement.executeUpdate();
        }
    }
}
