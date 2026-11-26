package hu.nye.progtech.command;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Set;
import java.util.regex.Matcher;

import hu.nye.progtech.model.Board;
import hu.nye.progtech.model.GamePhase;
import hu.nye.progtech.model.GameState;
import hu.nye.progtech.persistence.BoardFileRepository;
import hu.nye.progtech.persistence.BoardFormatException;
import hu.nye.progtech.ui.ConsoleView;

/**
 * Pálya betöltése szövegfájlból, a játék indítása előtt. Hiba esetén a régi pálya marad.
 */
public class LoadCommand extends Command {

    private final ConsoleView view;
    private final BoardFileRepository repository;

    public LoadCommand(ConsoleView view, BoardFileRepository repository) {
        super("load", "load <fájl>", "load\\s+(.+)", Set.of(GamePhase.SETUP));
        this.view = view;
        this.repository = repository;
    }

    @Override
    public void apply(GameState state, Matcher matcher) {
        String fileName = matcher.group(1).strip();
        try {
            Board loaded = repository.load(Path.of(fileName));
            state.getBoardService().loadBoard(loaded);
            view.println("Pálya betöltve: " + fileName);
            view.showBoard(loaded);
        } catch (IOException | InvalidPathException e) {
            view.println("A fájl nem olvasható: " + fileName);
        } catch (BoardFormatException | IllegalArgumentException e) {
            view.println("Hibás pálya: " + e.getMessage());
        }
    }
}
