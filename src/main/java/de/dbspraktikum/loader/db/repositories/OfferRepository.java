package de.dbspraktikum.loader.db.repositories;

import de.dbspraktikum.loader.db.Sql;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class OfferRepository {
    private final Connection connection;

    public OfferRepository(Connection connection) {
        this.connection = connection;
    }

    public void upsertOffer(int branchId, String asin, String state, BigDecimal price, String currency) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(Sql.UPSERT_OFFER)) {
            statement.setInt(1, branchId);
            statement.setString(2, asin);
            statement.setString(3, state);
            statement.setBigDecimal(4, price);
            statement.setBoolean(5, price != null);
            statement.setString(6, currency);
            statement.executeUpdate();
        }
    }
}
