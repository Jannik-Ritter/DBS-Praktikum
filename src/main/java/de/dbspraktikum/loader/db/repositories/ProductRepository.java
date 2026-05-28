package de.dbspraktikum.loader.db.repositories;

import de.dbspraktikum.loader.db.Sql;
import de.dbspraktikum.loader.error.ErrorLog;
import de.dbspraktikum.loader.error.Errors;
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

    public String typeOf(String productNumber) {
        return productTypes.get(productNumber);
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
                errors.record("Produkt", "Produkttyp", type, source + ":" + asin, Errors.productTypeMismatch(existingType));
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

    public void insertBook(
            String asin,
            Integer pages,
            LocalDate publication,
            String isbn,
            Integer publisherId,
            String source,
            ErrorLog errors
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(Sql.INSERT_BOOK)) {
            statement.setString(1, asin);
            JdbcUtil.setInteger(statement, 2, pages);
            JdbcUtil.setDate(statement, 3, publication);
            statement.setString(4, isbn);
            JdbcUtil.setInteger(statement, 5, publisherId);
            int changed = statement.executeUpdate();
            if (changed == 0) {
                errors.record("Buch", "Produktnummer", asin, source + ":" + asin, Errors.DUPLICATE_PRODUCT_SUBTYPE);
            }
        }
    }

    public void insertMusicCd(String asin, Integer labelId, LocalDate releaseDate, String source, ErrorLog errors) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(Sql.INSERT_MUSIC_CD)) {
            statement.setString(1, asin);
            JdbcUtil.setInteger(statement, 2, labelId);
            JdbcUtil.setDate(statement, 3, releaseDate);
            int changed = statement.executeUpdate();
            if (changed == 0) {
                errors.record("Musik-CD", "Produktnummer", asin, source + ":" + asin, Errors.DUPLICATE_PRODUCT_SUBTYPE);
            }
        }
    }

    public void insertDvd(
            String asin,
            String format,
            Integer runtimeMinutes,
            Integer regionCode,
            LocalDate releaseDate,
            String source,
            ErrorLog errors
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(Sql.INSERT_DVD)) {
            statement.setString(1, asin);
            statement.setString(2, format);
            statement.setString(3, runtimeMinutes == null ? null : runtimeMinutes + " minutes");
            JdbcUtil.setInteger(statement, 4, regionCode);
            JdbcUtil.setDate(statement, 5, releaseDate);
            int changed = statement.executeUpdate();
            if (changed == 0) {
                errors.record("DVD", "Produktnummer", asin, source + ":" + asin, Errors.DUPLICATE_PRODUCT_SUBTYPE);
            }
        }
    }

    public void insertTrack(String asin, String track, String source, ErrorLog errors) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(Sql.INSERT_TRACK)) {
            statement.setString(1, asin);
            statement.setString(2, track);
            int changed = statement.executeUpdate();
            if (changed == 0) {
                errors.record("Lied", "Name", track, source + ":" + asin, Errors.DUPLICATE_TRACK);
            }
        }
    }

    public void insertSimilarRefs(List<SimilarRef> refs, ErrorLog errors) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(Sql.INSERT_PRODUCT_SIMILARITY)) {
            for (SimilarRef ref : refs) {
                if (!Validation.validAsin(ref.similarProduct())) {
                    errors.record("Produktähnlichkeit", "Produktnummer2", ref.similarProduct(), ref.source() + ":" + ref.sourceProduct(), Errors.INVALID_PRODUCT_NUMBER);
                    continue;
                }
                if (!exists(ref.similarProduct())) {
                    errors.record("Produktähnlichkeit", "Produktnummer2", ref.similarProduct(), ref.source() + ":" + ref.sourceProduct(), Errors.SIMILAR_PRODUCT_NOT_FOUND);
                    continue;
                }
                if (ref.sourceProduct().equals(ref.similarProduct())) {
                    errors.record("Produktähnlichkeit", "Produktnummer2", ref.similarProduct(), ref.source() + ":" + ref.sourceProduct(), Errors.SELF_SIMILAR_PRODUCT);
                    continue;
                }

                String first = ref.sourceProduct().compareTo(ref.similarProduct()) < 0 ? ref.sourceProduct() : ref.similarProduct();
                String second = ref.sourceProduct().compareTo(ref.similarProduct()) < 0 ? ref.similarProduct() : ref.sourceProduct();
                statement.setString(1, first);
                statement.setString(2, second);
                statement.executeUpdate();
            }
        }
    }
}
