package hu.nye.progtech.command;

import java.util.Set;
import java.util.regex.Matcher;

import hu.nye.progtech.model.GamePhase;
import hu.nye.progtech.model.GameState;
import hu.nye.progtech.ui.ConsoleView;

/**
 * A játék indítása: SETUP fázisból PLAYING fázisba lép. A humán játékos kezd.
 */
public class StartCommand extends Command {

    private final ConsoleView view;

    public StartCommand(ConsoleView view) {
        super("start", "start", "start", Set.of(GamePhase.SETUP));
        this.view = view;
    }

    @Override
    public void apply(GameState state, Matcher matcher) {
        state.setPhase(GamePhase.PLAYING);
        view.println("A játék elindult, te kezdesz.");
        view.showBoard(state.getBoardService().getBoard());
    }
}
