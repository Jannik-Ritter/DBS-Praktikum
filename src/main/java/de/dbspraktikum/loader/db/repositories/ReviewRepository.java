package de.dbspraktikum.loader.db.repositories;

import de.dbspraktikum.loader.db.Sql;
import de.dbspraktikum.loader.error.ErrorLog;
import de.dbspraktikum.loader.error.Errors;
import de.dbspraktikum.loader.parse.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public final class ReviewRepository {
    private final Connection connection;

    public ReviewRepository(Connection connection) {
        this.connection = connection;
    }

    public void insertReview(
            int customerId,
            String asin,
            LocalDate date,
            Integer helpful,
            String summary,
            String content,
            int rating,
            String user,
            String source,
            ErrorLog errors
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(Sql.INSERT_REVIEW)) {
            statement.setInt(1, customerId);
            statement.setString(2, asin);
            JdbcUtil.setDate(statement, 3, date);
            JdbcUtil.setInteger(statement, 4, helpful);
            statement.setString(5, summary);
            statement.setString(6, content);
            statement.setInt(7, rating);

            int changed = statement.executeUpdate();
            if (changed == 0) {
                errors.record("Kundenrezension", "KundeID/Produktnummer", user + "/" + asin, source, Errors.DUPLICATE_REVIEW);
            }
        }
    }
}
