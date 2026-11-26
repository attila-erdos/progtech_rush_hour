package hu.nye.progtech;

import java.nio.file.Path;

import hu.nye.progtech.command.CommandRegistry;
import hu.nye.progtech.model.Board;
import hu.nye.progtech.model.GameState;
import hu.nye.progtech.persistence.BoardFileRepository;
import hu.nye.progtech.persistence.BoardTextFormat;
import hu.nye.progtech.persistence.DatabaseException;
import hu.nye.progtech.persistence.GameXmlRepository;
import hu.nye.progtech.persistence.HighScoreRepository;
import hu.nye.progtech.service.BoardService;
import hu.nye.progtech.service.GameEngine;
import hu.nye.progtech.service.StartingBoardLoader;
import hu.nye.progtech.ui.BoardRenderer;
import hu.nye.progtech.ui.ConsoleView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A program belépési pontja: összerakja az objektumokat és elindítja a játékot.
 * Az első parancssori argumentum a pályafájl (alapértelmezés: board.txt).
 */
public final class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static final String DEFAULT_BOARD_FILE = "board.txt";
    private static final String DATABASE_URL = "jdbc:h2:./rushhour-db";

    private Main() {
    }

    /**
     * @param args opcionálisan a pályafájl útvonala
     */
    public static void main(String[] args) {
        Path boardFile = Path.of(args.length > 0 ? args[0] : DEFAULT_BOARD_FILE);

        BoardFileRepository boardRepository = new BoardFileRepository(new BoardTextFormat());
        Board startingBoard = new StartingBoardLoader(boardRepository).load(boardFile);

        HighScoreRepository highScoreRepository = new HighScoreRepository(DATABASE_URL);
        try {
            highScoreRepository.initDatabase();
        } catch (DatabaseException e) {
            LOGGER.error("Az adatbázis nem érhető el", e);
        }

        ConsoleView view = new ConsoleView(System.in, System.out, new BoardRenderer());
        GameState state = new GameState(new BoardService(startingBoard));
        CommandRegistry registry = CommandRegistry.createDefault(view, boardRepository,
                new GameXmlRepository(), highScoreRepository);

        new GameEngine(view, registry, state, highScoreRepository).run();
    }
}
