package hu.nye.progtech.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import hu.nye.progtech.model.Board;
import hu.nye.progtech.model.Orientation;
import hu.nye.progtech.model.Position;
import hu.nye.progtech.model.Vehicle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BoardFileRepositoryTest {

    @TempDir
    Path tempDir;

    private final BoardFileRepository repository = new BoardFileRepository(new BoardTextFormat());

    @Test
    void savedBoardCanBeLoadedBack() throws IOException, BoardFormatException {
        Board board = new Board(2, 4, List.of(new Vehicle('A', new Position(1, 1), 2, Orientation.HORIZONTAL)));
        Path file = tempDir.resolve("palya.txt");

        repository.save(board, file);

        assertEquals(board, repository.load(file));
    }

    @Test
    void loadingMissingFileThrowsIoException() {
        assertThrows(IOException.class, () -> repository.load(tempDir.resolve("nincs.txt")));
    }
}
