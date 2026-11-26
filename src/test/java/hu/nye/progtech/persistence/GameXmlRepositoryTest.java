package hu.nye.progtech.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import hu.nye.progtech.model.Board;
import hu.nye.progtech.model.Orientation;
import hu.nye.progtech.model.Position;
import hu.nye.progtech.model.SavedGame;
import hu.nye.progtech.model.Vehicle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GameXmlRepositoryTest {

    private static final Board BOARD = new Board(3, 6, List.of(
            new Vehicle('A', new Position(0, 1), 2, Orientation.HORIZONTAL),
            new Vehicle('B', new Position(2, 2), 2, Orientation.HORIZONTAL),
            new Vehicle('C', new Position(0, 4), 3, Orientation.VERTICAL)));

    @TempDir
    Path tempDir;

    private final GameXmlRepository repository = new GameXmlRepository();

    private Path xml(String content) throws IOException {
        Path file = tempDir.resolve("game.xml");
        Files.writeString(file, content);
        return file;
    }

    @Test
    void savedGameCanBeLoadedBack() throws IOException, BoardFormatException {
        SavedGame game = new SavedGame("Erdős <Attila> & co", BOARD);
        Path file = tempDir.resolve("mentes.xml");

        repository.save(game, file);

        assertEquals(game, repository.load(file));
    }

    @Test
    void missingFileThrowsIoException() {
        assertThrows(IOException.class, () -> repository.load(tempDir.resolve("nincs.xml")));
    }

    @Test
    void saveToDirectoryThrowsIoException() {
        assertThrows(IOException.class, () -> repository.save(new SavedGame("Peti", BOARD), tempDir));
    }

    @Test
    void invalidContentIsRejected() throws IOException {
        List<String> invalid = List.of(
                "ez nem xml",
                "<?xml version=\"1.0\"?><!DOCTYPE game [<!ENTITY x \"y\">]><game/>",
                "<foo/>",
                "<game><board rows=\"1\" cols=\"4\"/></game>",
                "<game><player>P</player><board rows=\"x\" cols=\"4\"/></game>",
                "<game><player>P</player><board rows=\"1\" cols=\"4\">"
                        + "<vehicle id=\"AB\" row=\"0\" col=\"0\" length=\"2\" orientation=\"HORIZONTAL\"/></board></game>",
                "<game><player>P</player><board rows=\"1\" cols=\"4\">"
                        + "<vehicle id=\"A\" row=\"0\" col=\"0\" length=\"2\" orientation=\"DIAGONAL\"/></board></game>",
                "<game><player>P</player><board rows=\"1\" cols=\"4\">"
                        + "<vehicle id=\"A\" row=\"0\" col=\"3\" length=\"2\" orientation=\"HORIZONTAL\"/></board></game>");

        for (String content : invalid) {
            Path file = xml(content);
            assertThrows(BoardFormatException.class, () -> repository.load(file), content);
        }
    }
}
