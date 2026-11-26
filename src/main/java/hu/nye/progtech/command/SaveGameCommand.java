package hu.nye.progtech.command;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Set;
import java.util.regex.Matcher;

import hu.nye.progtech.model.GamePhase;
import hu.nye.progtech.model.GameState;
import hu.nye.progtech.model.SavedGame;
import hu.nye.progtech.persistence.GameXmlRepository;
import hu.nye.progtech.ui.ConsoleView;

/**
 * A folyamatban lévő játék mentése XML fájlba, hogy később onnan lehessen folytatni.
 */
public class SaveGameCommand extends Command {

    private final ConsoleView view;
    private final GameXmlRepository repository;

    public SaveGameCommand(ConsoleView view, GameXmlRepository repository) {
        super("savegame", "savegame <fájl.xml>", "savegame\\s+(.+)", Set.of(GamePhase.PLAYING));
        this.view = view;
        this.repository = repository;
    }

    @Override
    public void apply(GameState state, Matcher matcher) {
        String fileName = matcher.group(1).strip();
        SavedGame game = new SavedGame(state.getPlayerName(), state.getBoardService().getBoard());
        try {
            repository.save(game, Path.of(fileName));
            view.println("Játék elmentve: " + fileName);
        } catch (IOException | InvalidPathException e) {
            view.println("Nem sikerült menteni: " + fileName);
        }
    }
}
