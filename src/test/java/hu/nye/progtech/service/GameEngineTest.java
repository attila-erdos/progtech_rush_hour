package hu.nye.progtech.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import hu.nye.progtech.command.CommandRegistry;
import hu.nye.progtech.model.Board;
import hu.nye.progtech.model.GamePhase;
import hu.nye.progtech.model.GameState;
import hu.nye.progtech.model.HighScoreEntry;
import hu.nye.progtech.model.Orientation;
import hu.nye.progtech.model.Position;
import hu.nye.progtech.model.Vehicle;
import hu.nye.progtech.persistence.BoardFileRepository;
import hu.nye.progtech.persistence.BoardTextFormat;
import hu.nye.progtech.persistence.DatabaseException;
import hu.nye.progtech.persistence.GameXmlRepository;
import hu.nye.progtech.persistence.HighScoreRepository;
import hu.nye.progtech.ui.BoardRenderer;
import hu.nye.progtech.ui.ConsoleView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GameEngineTest {

    /*
     * ..AA..   az A 2 lépéssel nyer (jobb szél)
     * ..BB..   a B 2 lépéssel nyer (bal szél)
     */
    private static final Board BOARD = new Board(2, 6, List.of(
            new Vehicle('A', new Position(0, 2), 2, Orientation.HORIZONTAL),
            new Vehicle('B', new Position(1, 2), 2, Orientation.HORIZONTAL)));

    private static final BoardFileRepository REPOSITORY = new BoardFileRepository(new BoardTextFormat());

    @Mock
    private Random random;

    @Mock
    private HighScoreRepository highScores;

    @TempDir
    Path tempDir;

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    private GameState play(String input) {
        Charset charset = Charset.defaultCharset();
        ConsoleView view = new ConsoleView(new ByteArrayInputStream(input.getBytes(charset)),
                new PrintStream(output, true), new BoardRenderer());
        GameState state = new GameState(new BoardService(BOARD, random));
        CommandRegistry registry = CommandRegistry.createDefault(view, REPOSITORY, new GameXmlRepository(), highScores);
        new GameEngine(view, registry, state, highScores).run();
        return state;
    }

    private String output() {
        return output.toString(Charset.defaultCharset());
    }

    @Test
    void humanWinsByMovingHeroToTheExit() {
        GameState state = play("Peti\nstart\nmove A 2\n");

        assertEquals(Optional.of("Peti"), state.getWinnerName());
        assertEquals(GamePhase.GAME_ENDED, state.getPhase());
        assertTrue(output().contains("Peti nyert!"));
        verifyNoInteractions(random);
        verify(highScores).addPlayer("Peti");
        verify(highScores).recordWin("Peti");
    }

    @Test
    void machineMovesAutomaticallyAndCanWin() {
        // a gép a B-t választja (2 jármű közül az 1-es), és a 4 lehetséges lépéséből a -2-t
        when(random.nextInt(2)).thenReturn(1);
        when(random.nextInt(4)).thenReturn(3);

        GameState state = play("Peti\nstart\nmove A 1\n");

        assertTrue(output().contains("A gép lépése: move B -2"));
        assertEquals(Optional.of(GameState.MACHINE_NAME), state.getWinnerName());
        verify(highScores).recordWin(GameState.MACHINE_NAME);
    }

    @Test
    void movingOpponentsHeroOutMakesOpponentWin() {
        GameState state = play("Peti\nstart\nmove B -2\n");

        assertEquals(Optional.of(GameState.MACHINE_NAME), state.getWinnerName());
        verifyNoInteractions(random);
    }

    @Test
    void invalidMovesDoNotTriggerTheMachine() {
        play("Peti\nstart\nmove A 5\nmove X 1\nmove A 0\nquit\n");

        String out = output();
        assertTrue(out.contains("nem szabad az út"));
        assertTrue(out.contains("Nincs ilyen jármű"));
        assertTrue(out.contains("nem lehet 0"));
        verifyNoInteractions(random);
    }

    @Test
    void commandsAreCheckedAgainstThePhase() {
        GameState state = play("Peti\nmove A 1\nfoo\n\nquit\n");

        assertTrue(output().contains("Ez a parancs most nem használható."));
        assertTrue(output().contains("Ismeretlen parancs: foo"));
        assertTrue(output().contains("Parancsok: start | load <fájl> | save <fájl> | loadgame <fájl.xml> | show"));
        assertEquals(GamePhase.SETUP, state.getPhase());
    }

    @Test
    void endOfInputAndEmptyNameAreHandled() {
        GameState state = play("");

        assertTrue(state.isExitRequested());
        assertEquals(GameState.DEFAULT_PLAYER_NAME, state.getPlayerName());
    }

    @Test
    void loadReplacesTheBoardBeforeStart() throws IOException {
        Path file = tempDir.resolve("uj palya.txt");
        Files.write(file, List.of("1 6", ".BBAA."));

        GameState state = play("Peti\nload " + file + "\nquit\n");

        assertEquals(new Position(0, 3), state.getBoardService().getBoard().findVehicle('A').orElseThrow().start());
    }

    @Test
    void loadKeepsOldBoardOnError() throws IOException {
        Path invalid = tempDir.resolve("hibas.txt");
        Files.write(invalid, List.of("1 6", ".BB.AA"));
        Path broken = tempDir.resolve("tores.txt");
        Files.write(broken, List.of("x"));

        GameState state = play("Peti\nload " + tempDir.resolve("nincs.txt") + "\nload " + invalid
                + "\nload " + broken + "\nquit\n");

        assertEquals(BOARD, state.getBoardService().getBoard());
        assertTrue(output().contains("A fájl nem olvasható"));
        assertTrue(output().contains("Hibás pálya"));
    }

    @Test
    void saveWritesTheCurrentBoardAndReportsErrors() throws Exception {
        Path file = tempDir.resolve("mentes.txt");

        play("Peti\nstart\nsave " + file + "\nsave " + tempDir + "\nquit\n");

        assertEquals(BOARD, REPOSITORY.load(file));
        assertTrue(output().contains("Pálya elmentve"));
        assertTrue(output().contains("Nem sikerült menteni"));
    }

    @Test
    void newGameAfterTheEndGoesBackToSetupWithStarterBoard() {
        GameState state = play("Peti\nstart\nmove A 2\nshow\nnew\nquit\n");

        assertEquals(GamePhase.SETUP, state.getPhase());
        assertEquals(Optional.empty(), state.getWinnerName());
        assertEquals(BoardService.getStarterBoard(), state.getBoardService().getBoard());
    }

    @Test
    void tooLongNameIsShortened() {
        GameState state = play("x".repeat(50) + "\nquit\n");

        assertEquals("x".repeat(30), state.getPlayerName());
    }

    @Test
    void databaseErrorsDoNotStopTheGame() {
        doThrow(new DatabaseException("hiba", null)).when(highScores).addPlayer(anyString());
        doThrow(new DatabaseException("hiba", null)).when(highScores).recordWin(anyString());

        GameState state = play("Peti\nstart\nmove A 2\n");

        assertEquals(GamePhase.GAME_ENDED, state.getPhase());
        assertTrue(output().contains("az eredmények nem mentődnek"));
        assertTrue(output().contains("nem sikerült elmenteni"));
    }

    @Test
    void highScoreTableIsShown() {
        when(highScores.findTop(10)).thenReturn(List.of(new HighScoreEntry("Peti", 3), new HighScoreEntry("Gép", 1)));

        play("Peti\nhighscore\nquit\n");

        assertTrue(output().contains(" 1. Peti"));
        assertTrue(output().contains("3 győzelem"));
        assertTrue(output().contains(" 2. Gép"));
    }

    @Test
    void emptyHighScoreTableAndDatabaseErrorAreReported() {
        play("Peti\nhighscore\nquit\n");
        assertTrue(output().contains("Még nincs mentett játékos."));

        when(highScores.findTop(10)).thenThrow(new DatabaseException("Nem sikerült beolvasni", null));
        play("Peti\nhighscore\nquit\n");
        assertTrue(output().contains("Nem sikerült beolvasni"));
    }

    @Test
    void savedGameCanBeContinuedLater() {
        Path file = tempDir.resolve("mentes.xml");
        play("Peti\nstart\nsavegame " + file + "\nquit\n");

        GameState state = play("Zoli\nloadgame " + file + "\nquit\n");

        assertTrue(output().contains("Játék elmentve"));
        assertEquals(GamePhase.PLAYING, state.getPhase());
        assertEquals("Peti", state.getPlayerName());
        assertEquals(BOARD, state.getBoardService().getBoard());
    }

    @Test
    void saveGameAndLoadGameReportErrors() throws IOException {
        Path finished = tempDir.resolve("vege.xml");
        Files.writeString(finished, "<game><player>P</player><board rows=\"1\" cols=\"6\">"
                + "<vehicle id=\"B\" row=\"0\" col=\"0\" length=\"2\" orientation=\"HORIZONTAL\"/>"
                + "<vehicle id=\"A\" row=\"0\" col=\"2\" length=\"2\" orientation=\"HORIZONTAL\"/>"
                + "</board></game>");

        GameState state = play("Peti\nstart\nsavegame " + tempDir + "\nloadgame " + tempDir.resolve("nincs.xml")
                + "\nloadgame " + finished + "\nquit\n");

        assertTrue(output().contains("Nem sikerült menteni"));
        assertTrue(output().contains("A fájl nem olvasható"));
        assertTrue(output().contains("Hibás mentés"));
        assertEquals(BOARD, state.getBoardService().getBoard());
    }
}
