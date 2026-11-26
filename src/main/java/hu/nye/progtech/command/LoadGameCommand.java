package hu.nye.progtech.command;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.regex.Matcher;

import hu.nye.progtech.model.GamePhase;
import hu.nye.progtech.model.GameState;
import hu.nye.progtech.model.SavedGame;
import hu.nye.progtech.persistence.BoardFormatException;
import hu.nye.progtech.persistence.GameXmlRepository;
import hu.nye.progtech.ui.ConsoleView;

/**
 * Egy XML fájlba mentett játék visszatöltése: a játékos onnan folytatja, ahol abbahagyta.
 * Hiba esetén a régi állapot marad.
 */
public class LoadGameCommand extends Command {

    private final ConsoleView view;
    private final GameXmlRepository repository;

    public LoadGameCommand(ConsoleView view, GameXmlRepository repository) {
        super("loadgame", "loadgame <fájl.xml>", "loadgame\\s+(.+)", EnumSet.allOf(GamePhase.class));
        this.view = view;
        this.repository = repository;
    }

    @Override
    public void apply(GameState state, Matcher matcher) {
        String fileName = matcher.group(1).strip();
        try {
            SavedGame game = repository.load(Path.of(fileName));
            state.getBoardService().loadBoard(game.board());
            state.setPlayerName(game.playerName());
            state.setWinnerName(null);
            state.setPhase(GamePhase.PLAYING);
            view.println("Játék betöltve: " + fileName + ", folytasd, " + game.playerName() + "!");
            view.showBoard(game.board());
        } catch (IOException | InvalidPathException e) {
            view.println("A fájl nem olvasható: " + fileName);
        } catch (BoardFormatException | IllegalArgumentException e) {
            view.println("Hibás mentés: " + e.getMessage());
        }
    }
}
