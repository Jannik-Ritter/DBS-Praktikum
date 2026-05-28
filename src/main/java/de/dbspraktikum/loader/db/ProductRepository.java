package de.dbspraktikum.loader.db;

import de.dbspraktikum.loader.error.ErrorLog;
import de.dbspraktikum.loader.model.SimilarRef;
import de.dbspraktikum.loader.parse.JdbcUtil;
import de.dbspraktikum.loader.validation.Validation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Savepoint;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ProductRepository {
    private final Connection connection;
    private final Map<String, String> productTypes = new HashMap<>();

    public ProductRepository(Connection connection) {
        this.connection = connection;
    }

    public void loadExistingProducts() throws SQLException {
        productTypes.clear();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(Sql.SELECT_PRODUCT_TYPES)) {
            while (rs.next()) {
                productTypes.put(rs.getString(1), rs.getString(2));
            }
        }
    }

    public boolean exists(String productNumber) {
        return productTypes.containsKey(productNumber);
    }

    public boolean insertProduct(
            String asin,
            String title,
            String type,
            Integer salesRank,
            String picture,
            String detailPage,
            String ean,
            String source,
            ErrorLog errors
    ) throws SQLException {
        String existingType = productTypes.get(asin);
        if (existingType != null) {
            if (!existingType.equals(type)) {
                errors.record("Produkt", "Produkttyp", type, source + ":" + asin, "Produkt wurde bereits mit anderem Typ geladen: " + existingType);
                return false;
            }
            return true;
        }

        Savepoint savepoint = connection.setSavepoint();
        try (PreparedStatement statement = connection.prepareStatement(Sql.INSERT_PRODUCT)) {
            statement.setString(1, asin);
            statement.setString(2, title);
            JdbcUtil.setInteger(statement, 3, salesRank);
            statement.setString(4, picture);
            statement.setString(5, detailPage);
            statement.setString(6, ean);
            statement.setString(7, type);
            statement.executeUpdate();
            connection.releaseSavepoint(savepoint);
            productTypes.put(asin, type);
            return true;
        } catch (SQLException ex) {
            connection.rollback(savepoint);
            errors.record("Produkt", "Produktnummer", asin, source + ":" + asin, ex.getMessage());
            return false;
        }
    }

    public void insertBook(String asin, Integer pages, LocalDate publication, String isbn, Integer publisherId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(Sql.INSERT_BOOK)) {
            statement.setString(1, asin);
            JdbcUtil.setInteger(statement, 2, pages);
            JdbcUtil.setDate(statement, 3, publication);
            statement.setString(4, isbn);
            JdbcUtil.setInteger(statement, 5, publisherId);
            statement.executeUpdate();
        }
    }

    public void insertMusicCd(String asin, Integer labelId, LocalDate releaseDate) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(Sql.INSERT_MUSIC_CD)) {
            statement.setString(1, asin);
            JdbcUtil.setInteger(statement, 2, labelId);
            JdbcUtil.setDate(statement, 3, releaseDate);
            statement.executeUpdate();
        }
    }

    public void insertDvd(String asin, String format, Integer runtimeMinutes, Integer regionCode, LocalDate releaseDate) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(Sql.INSERT_DVD)) {
            statement.setString(1, asin);
            statement.setString(2, format);
            statement.setString(3, runtimeMinutes == null ? null : runtimeMinutes + " minutes");
            JdbcUtil.setInteger(statement, 4, regionCode);
            JdbcUtil.setDate(statement, 5, releaseDate);
            statement.executeUpdate();
        }
    }

    public void insertTrack(String asin, String track) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(Sql.INSERT_TRACK)) {
            statement.setString(1, asin);
            statement.setString(2, track);
            statement.executeUpdate();
        }
    }

    public void insertSimilarRefs(List<SimilarRef> refs, ErrorLog errors) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(Sql.INSERT_PRODUCT_SIMILARITY)) {
            for (SimilarRef ref : refs) {
                if (!Validation.validAsin(ref.target())) {
                    errors.record("Produktähnlichkeit", "Produktnummer2", ref.target(), ref.source() + ":" + ref.sourceProduct(), "Ungueltige ASIN");
                    continue;
                }
                if (!exists(ref.target())) {
                    errors.record("Produktähnlichkeit", "Produktnummer2", ref.target(), ref.source() + ":" + ref.sourceProduct(), "Aehnliches Produkt existiert nicht");
                    continue;
                }
                if (ref.sourceProduct().equals(ref.target())) {
                    errors.record("Produktähnlichkeit", "Produktnummer2", ref.target(), ref.source() + ":" + ref.sourceProduct(), "Produkt darf nicht sich selbst aehnlich sein");
                    continue;
                }

                String first = ref.sourceProduct().compareTo(ref.target()) < 0 ? ref.sourceProduct() : ref.target();
                String second = ref.sourceProduct().compareTo(ref.target()) < 0 ? ref.target() : ref.sourceProduct();
                statement.setString(1, first);
                statement.setString(2, second);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }
}
