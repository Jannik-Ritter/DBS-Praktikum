package de.dbspraktikum.loader.db.repositories;

import de.dbspraktikum.loader.db.Sql;
import de.dbspraktikum.loader.error.LoadError;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class ErrorRepository {
    private final Connection connection;

    public ErrorRepository(Connection connection) {
        this.connection = connection;
    }

    public void insert(LoadError error) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(Sql.INSERT_LOAD_ERROR)) {
            statement.setString(1, error.entity());
            statement.setString(2, error.attribute());
            statement.setString(3, error.rawValue());
            statement.setString(4, error.source());
            statement.setString(5, error.message());
            statement.executeUpdate();
        }
    }
}
