package hu.nye.progtech.command;

import java.util.Set;
import java.util.regex.Matcher;

import hu.nye.progtech.model.GamePhase;
import hu.nye.progtech.model.GameState;
import hu.nye.progtech.service.BoardService;
import hu.nye.progtech.ui.ConsoleView;

/**
 * Új játék a játszma vége után: visszaáll az alap pályára és a SETUP fázisba.
 */
public class NewGameCommand extends Command {

    private final ConsoleView view;

    public NewGameCommand(ConsoleView view) {
        super("new", "new", "new", Set.of(GamePhase.GAME_ENDED));
        this.view = view;
    }

    @Override
    public void apply(GameState state, Matcher matcher) {
        state.getBoardService().loadBoard(BoardService.getStarterBoard());
        state.setWinnerName(null);
        state.setPhase(GamePhase.SETUP);
        view.println("Új játék: az alap pálya betöltve. Indítás: start");
    }
}
