package hu.nye.progtech.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;

import hu.nye.progtech.command.CommandRegistry.MatchedCommand;
import hu.nye.progtech.model.GamePhase;
import hu.nye.progtech.persistence.BoardFileRepository;
import hu.nye.progtech.persistence.BoardTextFormat;
import hu.nye.progtech.persistence.GameXmlRepository;
import hu.nye.progtech.persistence.HighScoreRepository;
import hu.nye.progtech.ui.BoardRenderer;
import hu.nye.progtech.ui.ConsoleView;
import org.junit.jupiter.api.Test;

class CommandRegistryTest {

    private final ConsoleView view = new ConsoleView(new ByteArrayInputStream(new byte[0]),
            new PrintStream(new ByteArrayOutputStream()), new BoardRenderer());
    private final CommandRegistry registry =
            CommandRegistry.createDefault(view, new BoardFileRepository(new BoardTextFormat()),
                    new GameXmlRepository(), new HighScoreRepository("jdbc:h2:mem:registry"));

    private List<String> namesIn(GamePhase phase) {
        return registry.availableCommands(phase).stream().map(Command::getName).toList();
    }

    @Test
    void eachPhaseOffersItsOwnCommands() {
        assertEquals(List.of("start", "load", "save", "loadgame", "show", "highscore", "quit"),
                namesIn(GamePhase.SETUP));
        assertEquals(List.of("move", "save", "savegame", "loadgame", "show", "highscore", "quit"),
                namesIn(GamePhase.PLAYING));
        assertEquals(List.of("loadgame", "show", "highscore", "new", "quit"), namesIn(GamePhase.GAME_ENDED));
    }

    @Test
    void moveIsFoundWithItsParametersIgnoringCase() {
        Optional<MatchedCommand> found = registry.findCommand("  MOVE c -12 ", GamePhase.PLAYING);

        assertTrue(found.isPresent());
        assertEquals("move", found.get().command().getName());
        assertEquals("c", found.get().matcher().group(1));
        assertEquals("-12", found.get().matcher().group(2));
    }

    @Test
    void fileNameMayContainSpaces() {
        Optional<MatchedCommand> found = registry.findCommand("save my board.txt", GamePhase.SETUP);

        assertEquals("my board.txt", found.orElseThrow().matcher().group(1));
    }

    @Test
    void commandIsNotFoundInWrongPhase() {
        assertTrue(registry.findCommand("move A 1", GamePhase.SETUP).isEmpty());
        assertTrue(registry.matchesAnyCommand("move A 1"));
    }

    @Test
    void malformedInputMatchesNothing() {
        for (String input : List.of("move", "move CD 1", "move C x", "move C 1234", "save", "jump")) {
            assertTrue(registry.findCommand(input, GamePhase.PLAYING).isEmpty(), input);
            assertFalse(registry.matchesAnyCommand(input), input);
        }
    }

    @Test
    void saveAndSaveGameAreDistinguished() {
        assertEquals("save", registry.findCommand("save a.txt", GamePhase.PLAYING).orElseThrow().command().getName());
        assertEquals("savegame",
                registry.findCommand("savegame a.xml", GamePhase.PLAYING).orElseThrow().command().getName());
    }

    @Test
    void exitIsAnAliasForQuit() {
        assertEquals("quit", registry.findCommand("exit", GamePhase.GAME_ENDED).orElseThrow().command().getName());
    }
}
