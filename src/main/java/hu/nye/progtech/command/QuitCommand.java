package hu.nye.progtech.command;

import java.util.EnumSet;
import java.util.regex.Matcher;

import hu.nye.progtech.model.GamePhase;
import hu.nye.progtech.model.GameState;

/**
 * Kilépés a programból. Bármelyik fázisban használható.
 */
public class QuitCommand extends Command {

    public QuitCommand() {
        super("quit", "quit", "quit|exit", EnumSet.allOf(GamePhase.class));
    }

    @Override
    public void apply(GameState state, Matcher matcher) {
        state.setExitRequested(true);
    }
}
