package hu.nye.progtech.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import hu.nye.progtech.model.HighScoreEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HighScoreRepositoryTest {

    private HighScoreRepository repository;

    @BeforeEach
    void setUp() {
        // minden teszt saját, memóriában lévő H2 adatbázist kap
        repository = new HighScoreRepository("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        repository.initDatabase();
    }

    @Test
    void initDatabaseCanBeCalledAgain() {
        repository.initDatabase();

        assertEquals(List.of(), repository.findTop(10));
    }

    @Test
    void playerIsAddedOnlyOnceWithZeroWins() {
        repository.addPlayer("Peti");
        repository.addPlayer("Peti");

        assertEquals(List.of(new HighScoreEntry("Peti", 0)), repository.findTop(10));
    }

    @Test
    void recordWinIncrementsExistingAndInsertsNewPlayer() {
        repository.addPlayer("Peti");
        repository.recordWin("Peti");
        repository.recordWin("Peti");
        repository.recordWin("Gép");

        assertEquals(List.of(new HighScoreEntry("Peti", 2), new HighScoreEntry("Gép", 1)), repository.findTop(10));
    }

    @Test
    void findTopOrdersByWinsThenNameAndRespectsLimit() {
        repository.recordWin("Bea");
        repository.recordWin("Anna");
        repository.recordWin("Cili");
        repository.recordWin("Cili");

        assertEquals(List.of(new HighScoreEntry("Cili", 2), new HighScoreEntry("Anna", 1)), repository.findTop(2));
    }

    @Test
    void unreachableDatabaseThrowsDatabaseException() {
        HighScoreRepository broken = new HighScoreRepository("jdbc:nincs:ilyen");

        assertThrows(DatabaseException.class, broken::initDatabase);
        assertThrows(DatabaseException.class, () -> broken.addPlayer("Peti"));
        assertThrows(DatabaseException.class, () -> broken.recordWin("Peti"));
        assertThrows(DatabaseException.class, () -> broken.findTop(10));
    }
}
