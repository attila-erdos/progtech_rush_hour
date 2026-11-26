package hu.nye.progtech.command;

import java.util.EnumSet;
import java.util.regex.Matcher;

import hu.nye.progtech.model.GamePhase;
import hu.nye.progtech.model.GameState;
import hu.nye.progtech.ui.ConsoleView;

/**
 * Az aktuális tábla kiírása. Bármelyik fázisban használható.
 */
public class ShowCommand extends Command {

    private final ConsoleView view;

    public ShowCommand(ConsoleView view) {
        super("show", "show", "show", EnumSet.allOf(GamePhase.class));
        this.view = view;
    }

    @Override
    public void apply(GameState state, Matcher matcher) {
        view.showBoard(state.getBoardService().getBoard());
    }
}
