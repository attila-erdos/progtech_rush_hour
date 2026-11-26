package hu.nye.progtech.service;

import java.util.Optional;
import java.util.stream.Collectors;

import hu.nye.progtech.command.Command;
import hu.nye.progtech.command.CommandRegistry;
import hu.nye.progtech.command.CommandRegistry.MatchedCommand;
import hu.nye.progtech.model.GamePhase;
import hu.nye.progtech.model.GameState;
import hu.nye.progtech.model.VehicleType;
import hu.nye.progtech.persistence.DatabaseException;
import hu.nye.progtech.persistence.HighScoreRepository;
import hu.nye.progtech.ui.ConsoleView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A játék fő ciklusa (az MVC-ben a controller). Minden körben:
 * <ol>
 *     <li>kiírja az aktuális fázisban használható parancsokat,</li>
 *     <li>beolvas egy sort,</li>
 *     <li>megkeresi az illeszkedő és ebben a fázisban engedélyezett parancsot,</li>
 *     <li>végrehajtja,</li>
 *     <li>játék közben megnézi, van-e győztes, és a győzelmet elmenti az adatbázisba.</li>
 * </ol>
 */
public class GameEngine implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameEngine.class);
    private static final int MAX_NAME_LENGTH = 30;

    private static final String RULES = String.join(System.lineSeparator(),
            "Te az A autóval vagy, a jobb szélen kell kijutnod (>). A gép a B autóval, a bal szélen (<).",
            "Bármelyik járművet csúsztathatod a saját tengelye mentén:",
            "  move C 2 = 2 mezőt jobbra/le, move C -1 = 1 mezőt balra/fel.");

    private final ConsoleView view;
    private final CommandRegistry registry;
    private final GameState state;
    private final HighScoreRepository highScoreRepository;

    /**
     * @param view a konzolos nézet
     * @param registry a parancsok
     * @param state a játék állapota
     * @param highScoreRepository a dicsőséglista adatbázisa
     */
    public GameEngine(ConsoleView view, CommandRegistry registry, GameState state,
                      HighScoreRepository highScoreRepository) {
        this.view = view;
        this.registry = registry;
        this.state = state;
        this.highScoreRepository = highScoreRepository;
    }

    @Override
    public void run() {
        view.println("=== Rush Hour - kétszemélyes változat ===");
        askPlayerName();
        view.println(RULES);
        view.showBoard(state.getBoardService().getBoard());

        while (!state.isExitRequested()) {
            showAvailableCommands();
            view.print(state.getPlayerName() + "> ");
            String line = view.readLine();
            if (line == null) {
                state.setExitRequested(true);
            } else if (!line.isBlank()) {
                execute(line);
            }
        }
        view.println("Viszlát!");
    }

    private void askPlayerName() {
        view.print("Add meg a neved: ");
        String name = view.readLine();
        if (name != null && !name.isBlank()) {
            String stripped = name.strip();
            state.setPlayerName(stripped.substring(0, Math.min(stripped.length(), MAX_NAME_LENGTH)));
        }
        LOGGER.info("Játékos: {}", state.getPlayerName());
        try {
            highScoreRepository.addPlayer(state.getPlayerName());
        } catch (DatabaseException e) {
            LOGGER.error("A játékos mentése nem sikerült", e);
            view.println("Figyelem: az adatbázis nem érhető el, az eredmények nem mentődnek.");
        }
    }

    private void showAvailableCommands() {
        String commands = registry.availableCommands(state.getPhase()).stream()
                .map(Command::getSyntax)
                .collect(Collectors.joining(" | "));
        view.println("Parancsok: " + commands);
    }

    private void execute(String line) {
        Optional<MatchedCommand> found = registry.findCommand(line, state.getPhase());
        if (found.isEmpty()) {
            view.println(registry.matchesAnyCommand(line)
                    ? "Ez a parancs most nem használható."
                    : "Ismeretlen parancs: " + line.strip());
            return;
        }
        LOGGER.debug("Parancs: {}", line.strip());
        found.get().command().apply(state, found.get().matcher());
        if (state.getPhase() == GamePhase.PLAYING) {
            checkEndOfGame();
        }
    }

    private void checkEndOfGame() {
        Optional<VehicleType> winner = state.getBoardService().winner();
        if (winner.isEmpty()) {
            return;
        }
        String winnerName = winner.get() == VehicleType.HERO_A ? state.getPlayerName() : GameState.MACHINE_NAME;
        state.setWinnerName(winnerName);
        state.setPhase(GamePhase.GAME_ENDED);
        LOGGER.info("Győztes: {}", winnerName);
        view.println(winnerName + " nyert! Új játék: new, dicsőséglista: highscore");
        try {
            highScoreRepository.recordWin(winnerName);
        } catch (DatabaseException e) {
            LOGGER.error("A győzelem mentése nem sikerült", e);
            view.println("A győzelmet nem sikerült elmenteni az adatbázisba.");
        }
    }
}
