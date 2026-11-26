package hu.nye.progtech.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import hu.nye.progtech.model.Board;
import hu.nye.progtech.model.Orientation;
import hu.nye.progtech.model.Position;
import hu.nye.progtech.model.Vehicle;
import hu.nye.progtech.persistence.BoardFileRepository;
import hu.nye.progtech.persistence.BoardFormatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StartingBoardLoaderTest {

    private static final Path PATH = Path.of("palya.txt");

    @Mock
    private BoardFileRepository repository;

    private StartingBoardLoader loader;

    @BeforeEach
    void setUp() {
        loader = new StartingBoardLoader(repository);
    }

    @Test
    void validFileIsUsed() throws IOException, BoardFormatException {
        Board fromFile = new Board(1, 6, List.of(
                new Vehicle('B', new Position(0, 1), 2, Orientation.HORIZONTAL),
                new Vehicle('A', new Position(0, 3), 2, Orientation.HORIZONTAL)));
        when(repository.load(PATH)).thenReturn(fromFile);

        assertEquals(fromFile, loader.load(PATH));
    }

    @Test
    void missingFileFallsBackToDefault() throws IOException, BoardFormatException {
        when(repository.load(PATH)).thenThrow(new IOException("nincs ilyen fájl"));

        assertEquals(BoardService.getStarterBoard(), loader.load(PATH));
    }

    @Test
    void badFormatFallsBackToDefault() throws IOException, BoardFormatException {
        when(repository.load(PATH)).thenThrow(new BoardFormatException("hibás"));

        assertEquals(BoardService.getStarterBoard(), loader.load(PATH));
    }

    @Test
    void unplayableBoardFallsBackToDefault() throws IOException, BoardFormatException {
        Board withoutHeroes = new Board(1, 6, List.of(
                new Vehicle('C', new Position(0, 1), 2, Orientation.HORIZONTAL)));
        when(repository.load(PATH)).thenReturn(withoutHeroes);

        assertEquals(BoardService.getStarterBoard(), loader.load(PATH));
    }
}
