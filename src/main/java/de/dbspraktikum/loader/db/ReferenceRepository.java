package de.dbspraktikum.loader.db;

import de.dbspraktikum.loader.parse.JdbcUtil;
import de.dbspraktikum.loader.parse.TextUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public final class ReferenceRepository {
    private final Connection connection;
    private final Map<String, Integer> publishers = new HashMap<>();
    private final Map<String, Integer> labels = new HashMap<>();
    private final Map<String, Integer> persons = new HashMap<>();
    private final Map<String, Integer> customers = new HashMap<>();

    public ReferenceRepository(Connection connection) {
        this.connection = connection;
    }

    public int upsertBranch(String name, String street, String zip) throws SQLException {
        String branchName = TextUtil.firstNonBlank(name, "Unbekannte Filiale");
        String address = String.join(", ", java.util.Arrays.stream(new String[]{street, zip}).filter(java.util.Objects::nonNull).toList());
        if (address.isBlank()) {
            address = "unbekannt";
        }
        try (PreparedStatement statement = connection.prepareStatement(Sql.UPSERT_BRANCH)) {
            statement.setString(1, branchName);
            statement.setString(2, address);
            statement.setString(3, branchName);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public int publisherId(String name) throws SQLException {
        return lookupId(publishers, name, Sql.UPSERT_PUBLISHER);
    }

    public int labelId(String name) throws SQLException {
        return lookupId(labels, name, Sql.UPSERT_LABEL);
    }

    public int personId(String name) throws SQLException {
        return lookupId(persons, name, Sql.UPSERT_PERSON);
    }

    public int customerId(String name) throws SQLException {
        return lookupId(customers, name, Sql.UPSERT_CUSTOMER);
    }

    public void insertRole(int personId, String role) throws SQLException {
        String sql = switch (role) {
            case "Actor" -> Sql.INSERT_ACTOR_ROLE;
            case "Autor" -> Sql.INSERT_AUTHOR_ROLE;
            case "Creator" -> Sql.INSERT_CREATOR_ROLE;
            case "Director" -> Sql.INSERT_DIRECTOR_ROLE;
            case "Künstler" -> Sql.INSERT_ARTIST_ROLE;
            default -> throw new IllegalArgumentException("Unbekannte Rolle: " + role);
        };
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, personId);
            statement.executeUpdate();
        }
    }

    public void insertBookAuthor(String asin, int personId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(Sql.INSERT_BOOK_AUTHOR)) {
            statement.setString(1, asin);
            statement.setInt(2, personId);
            statement.executeUpdate();
        }
    }

    public void insertMusicArtist(String asin, int personId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(Sql.INSERT_MUSIC_ARTIST)) {
            statement.setString(1, asin);
            statement.setInt(2, personId);
            statement.executeUpdate();
        }
    }

    public void insertDvdParticipant(String asin, int personId, String role) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(Sql.INSERT_DVD_PARTICIPANT)) {
            statement.setString(1, asin);
            statement.setInt(2, personId);
            statement.setString(3, role);
            statement.executeUpdate();
        }
    }

    private int lookupId(Map<String, Integer> cache, String rawName, String sql) throws SQLException {
        String name = TextUtil.clean(rawName);
        if (name == null) {
            throw new IllegalArgumentException("Name darf nicht leer sein");
        }
        Integer existing = cache.get(name);
        if (existing != null) {
            return existing;
        }
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                int id = rs.getInt(1);
                cache.put(name, id);
                return id;
            }
        }
    }
}
