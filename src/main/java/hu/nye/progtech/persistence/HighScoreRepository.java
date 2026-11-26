package hu.nye.progtech.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import hu.nye.progtech.model.HighScoreEntry;

/**
 * A játékosok nevének és győzelmeik számának tárolása adatbázisban, JDBC-vel.
 * A program H2 beágyazott adatbázist használ, ehhez nem kell külön szerver.
 */
public class HighScoreRepository {

    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS players (name VARCHAR(100) PRIMARY KEY, wins INT NOT NULL)";
    private static final String FIND_PLAYER = "SELECT name FROM players WHERE name = ?";
    private static final String INSERT_PLAYER = "INSERT INTO players (name, wins) VALUES (?, ?)";
    private static final String INCREMENT_WINS = "UPDATE players SET wins = wins + 1 WHERE name = ?";
    private static final String FIND_ALL = "SELECT name, wins FROM players ORDER BY wins DESC, name";

    private final String jdbcUrl;

    /**
     * @param jdbcUrl az adatbázis címe, pl. {@code jdbc:h2:./rushhour-db}
     */
    public HighScoreRepository(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, USER, PASSWORD);
    }

    /**
     * Létrehozza a táblát, ha még nem létezik.
     */
    public void initDatabase() {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.execute(CREATE_TABLE);
        } catch (SQLException e) {
            throw new DatabaseException("Nem sikerült létrehozni az adatbázist.", e);
        }
    }

    /**
     * Felveszi a játékost 0 győzelemmel, ha még nincs az adatbázisban.
     *
     * @param playerName a játékos neve
     */
    public void addPlayer(String playerName) {
        try (Connection connection = connect()) {
            if (!exists(connection, playerName)) {
                insert(connection, playerName, 0);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Nem sikerült menteni a játékost.", e);
        }
    }

    /**
     * Eggyel növeli a játékos győzelmeinek számát (ha még nincs az adatbázisban, felveszi).
     *
     * @param playerName a győztes neve
     */
    public void recordWin(String playerName) {
        try (Connection connection = connect();
             PreparedStatement update = connection.prepareStatement(INCREMENT_WINS)) {
            update.setString(1, playerName);
            if (update.executeUpdate() == 0) {
                insert(connection, playerName, 1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Nem sikerült menteni a győzelmet.", e);
        }
    }

    /**
     * @param limit legfeljebb hány sort adjon vissza
     * @return a játékosok a győzelmek száma szerint csökkenő sorrendben (egyezésnél név szerint)
     */
    public List<HighScoreEntry> findTop(int limit) {
        try (Connection connection = connect();
             PreparedStatement query = connection.prepareStatement(FIND_ALL)) {
            query.setMaxRows(limit);
            List<HighScoreEntry> entries = new ArrayList<>();
            try (ResultSet result = query.executeQuery()) {
                while (result.next()) {
                    entries.add(new HighScoreEntry(result.getString("name"), result.getInt("wins")));
                }
            }
            return entries;
        } catch (SQLException e) {
            throw new DatabaseException("Nem sikerült beolvasni a dicsőséglistát.", e);
        }
    }

    private boolean exists(Connection connection, String playerName) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(FIND_PLAYER)) {
            query.setString(1, playerName);
            try (ResultSet result = query.executeQuery()) {
                return result.next();
            }
        }
    }

    private void insert(Connection connection, String playerName, int wins) throws SQLException {
        try (PreparedStatement insert = connection.prepareStatement(INSERT_PLAYER)) {
            insert.setString(1, playerName);
            insert.setInt(2, wins);
            insert.executeUpdate();
        }
    }
}
