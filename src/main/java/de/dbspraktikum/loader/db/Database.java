package de.dbspraktikum.loader.db;

import de.dbspraktikum.loader.app.LoaderConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class Database implements AutoCloseable {
    private final Connection connection;

    private Database(Connection connection) {
        this.connection = connection;
    }

    public static Database connect(LoaderConfig config) throws SQLException {
        Properties dbProperties = new Properties();
        dbProperties.setProperty("user", config.dbUser());
        dbProperties.setProperty("password", config.dbPassword());

        Connection connection = DriverManager.getConnection(config.dbUrl(), dbProperties);
        connection.setAutoCommit(false);

        return new Database(connection);
    }

    public Connection connection() {
        return connection;
    }

    public void commit() throws SQLException {
        connection.commit();
    }

    public void rollback() throws SQLException {
        connection.rollback();
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }

    public void executeSchema(Path schema) throws IOException, SQLException {
        String sql = Files.readString(schema, StandardCharsets.UTF_8);
        try (Statement statement = connection.createStatement()) {
            for (String command : splitSql(sql)) {
                if (!command.isBlank()) {
                    statement.execute(command);
                }
            }
        }
        connection.commit();
    }

    public long countProducts() throws SQLException {
        return count(Sql.COUNT_PRODUCTS);
    }

    public long countReviews() throws SQLException {
        return count(Sql.COUNT_REVIEWS);
    }

    public long countCategories() throws SQLException {
        return count(Sql.COUNT_CATEGORIES);
    }

    public long countLoadErrors() throws SQLException {
        return count(Sql.COUNT_LOAD_ERRORS);
    }

    private long count(String sql) throws SQLException {
        try (
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql)
            ) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private static List<String> splitSql(String sql) {
        List<String> commands = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean inLineComment = false;
        int blockCommentDepth = 0;
        String dollarTag = null;

        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);

            if (inLineComment) {
                current.append(c);
                if (c == '\n' || c == '\r') {
                    inLineComment = false;
                }
                continue;
            }

            if (blockCommentDepth > 0) {
                current.append(c);
                if (c == '/' && i + 1 < sql.length() && sql.charAt(i + 1) == '*') {
                    current.append(sql.charAt(++i));
                    blockCommentDepth++;
                } else if (c == '*' && i + 1 < sql.length() && sql.charAt(i + 1) == '/') {
                    current.append(sql.charAt(++i));
                    blockCommentDepth--;
                }
                continue;
            }

            if (dollarTag != null) {
                current.append(c);
                if (sql.startsWith(dollarTag, i)) {
                    current.append(sql, i + 1, i + dollarTag.length());
                    i += dollarTag.length() - 1;
                    dollarTag = null;
                }
                continue;
            }

            if (!inSingleQuote && !inDoubleQuote && c == '-'
                && i + 1 < sql.length() && sql.charAt(i + 1) == '-') {
                current.append(c).append(sql.charAt(++i));
                inLineComment = true;
                continue;
            }

            if (!inSingleQuote && !inDoubleQuote && c == '/'
                && i + 1 < sql.length() && sql.charAt(i + 1) == '*') {
                current.append(c).append(sql.charAt(++i));
                blockCommentDepth = 1;
                continue;
            }

            if (!inSingleQuote && !inDoubleQuote && c == '$') {
                int end = sql.indexOf('$', i + 1);
                if (end > i) {
                    String candidate = sql.substring(i, end + 1);
                    if (candidate.matches("\\$[A-Za-z0-9_]*\\$")) {
                        dollarTag = candidate;
                        current.append(candidate);
                        i = end;
                        continue;
                    }
                }
            }

            if (!inDoubleQuote && c == '\'') {
                current.append(c);
                if (inSingleQuote && i + 1 < sql.length() && sql.charAt(i + 1) == '\'') {
                    current.append(sql.charAt(++i));
                    continue;
                }
                inSingleQuote = !inSingleQuote;
                continue;
            }

            if (!inSingleQuote && c == '"') {
                inDoubleQuote = !inDoubleQuote;
                current.append(c);
                continue;
            }

            if (!inSingleQuote && !inDoubleQuote && c == ';') {
                commands.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        if (!current.toString().isBlank()) {
            commands.add(current.toString().trim());
        }
        return commands;
    }
}
