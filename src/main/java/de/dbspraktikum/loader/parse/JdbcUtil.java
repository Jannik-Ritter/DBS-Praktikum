package de.dbspraktikum.loader.parse;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public final class JdbcUtil {
    private JdbcUtil() {
    }

    public static void setInteger(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(index, java.sql.Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }

    public static void setDate(PreparedStatement statement, int index, LocalDate value) throws SQLException {
        if (value == null) {
            statement.setNull(index, java.sql.Types.DATE);
        } else {
            statement.setObject(index, value);
        }
    }
}
