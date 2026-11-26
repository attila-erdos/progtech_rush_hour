package hu.nye.progtech.command;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Set;
import java.util.regex.Matcher;

import hu.nye.progtech.model.GamePhase;
import hu.nye.progtech.model.GameState;
import hu.nye.progtech.persistence.BoardFileRepository;
import hu.nye.progtech.ui.ConsoleView;

/**
 * Az aktuális pálya mentése szövegfájlba.
 */
public class SaveCommand extends Command {

    private final ConsoleView view;
    private final BoardFileRepository repository;

    public SaveCommand(ConsoleView view, BoardFileRepository repository) {
        super("save", "save <fájl>", "save\\s+(.+)", Set.of(GamePhase.SETUP, GamePhase.PLAYING));
        this.view = view;
        this.repository = repository;
    }

    @Override
    public void apply(GameState state, Matcher matcher) {
        String fileName = matcher.group(1).strip();
        try {
            repository.save(state.getBoardService().getBoard(), Path.of(fileName));
            view.println("Pálya elmentve: " + fileName);
        } catch (IOException | InvalidPathException e) {
            view.println("Nem sikerült menteni: " + fileName);
        }
    }
}
