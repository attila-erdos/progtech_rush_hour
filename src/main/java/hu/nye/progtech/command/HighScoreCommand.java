package hu.nye.progtech.command;

import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;

import hu.nye.progtech.model.GamePhase;
import hu.nye.progtech.model.GameState;
import hu.nye.progtech.model.HighScoreEntry;
import hu.nye.progtech.persistence.DatabaseException;
import hu.nye.progtech.persistence.HighScoreRepository;
import hu.nye.progtech.ui.ConsoleView;

/**
 * A dicsőséglista kiírása: melyik játékos hány meccset nyert. Bármelyik fázisban használható.
 */
public class HighScoreCommand extends Command {

    private static final int LIMIT = 10;

    private final ConsoleView view;
    private final HighScoreRepository repository;

    public HighScoreCommand(ConsoleView view, HighScoreRepository repository) {
        super("highscore", "highscore", "highscore", EnumSet.allOf(GamePhase.class));
        this.view = view;
        this.repository = repository;
    }

    @Override
    public void apply(GameState state, Matcher matcher) {
        List<HighScoreEntry> entries;
        try {
            entries = repository.findTop(LIMIT);
        } catch (DatabaseException e) {
            view.println(e.getMessage());
            return;
        }
        view.println("--- Dicsőséglista ---");
        if (entries.isEmpty()) {
            view.println("Még nincs mentett játékos.");
            return;
        }
        for (int i = 0; i < entries.size(); i++) {
            HighScoreEntry entry = entries.get(i);
            view.println(String.format("%2d. %-20s %d győzelem", i + 1, entry.playerName(), entry.wins()));
        }
    }
}
