package hu.nye.progtech.service;

import java.io.IOException;
import java.nio.file.Path;

import hu.nye.progtech.model.Board;
import hu.nye.progtech.persistence.BoardFileRepository;
import hu.nye.progtech.persistence.BoardFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A kezdőállás betöltése: a megadott fájlból, vagy ha az hiányzik, hibás,
 * illetve nem játszható, akkor a beégetett alap pályáról.
 */
public class StartingBoardLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(StartingBoardLoader.class);

    private final BoardFileRepository repository;

    public StartingBoardLoader(BoardFileRepository repository) {
        this.repository = repository;
    }

    /**
     * @param path a pályafájl útvonala
     * @return a fájlból betöltött tábla, vagy az alap pálya
     */
    public Board load(Path path) {
        try {
            Board board = repository.load(path);
            BoardService.validateStartingBoard(board);
            LOGGER.info("Pálya betöltve: {}", path);
            return board;
        } catch (IOException e) {
            LOGGER.warn("A pályafájl nem olvasható ({}), az alap pályával indulunk.", path);
        } catch (BoardFormatException | IllegalArgumentException e) {
            LOGGER.warn("A pályafájl hibás ({}), az alap pályával indulunk.", e.getMessage());
        }
        return BoardService.getStarterBoard();
    }
}
