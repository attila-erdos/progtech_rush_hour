package hu.nye.progtech.command;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

import hu.nye.progtech.model.GamePhase;
import hu.nye.progtech.persistence.BoardFileRepository;
import hu.nye.progtech.persistence.GameXmlRepository;
import hu.nye.progtech.persistence.HighScoreRepository;
import hu.nye.progtech.ui.ConsoleView;

/**
 * A program összes parancsa. Megkeresi, melyik parancs illeszkedik a beírt sorra az adott fázisban.
 */
public class CommandRegistry {

    private final List<Command> commands;

    /**
     * @param commands a regisztrált parancsok
     */
    public CommandRegistry(List<Command> commands) {
        this.commands = List.copyOf(commands);
    }

    /**
     * @param view a konzolos nézet
     * @param boardRepository a szöveges pályafájlok kezelője
     * @param gameXmlRepository az XML-be mentett játékok kezelője
     * @param highScoreRepository a dicsőséglista adatbázisa
     * @return a játék összes parancsát tartalmazó regiszter
     */
    public static CommandRegistry createDefault(ConsoleView view, BoardFileRepository boardRepository,
                                                GameXmlRepository gameXmlRepository,
                                                HighScoreRepository highScoreRepository) {
        return new CommandRegistry(List.of(
                new StartCommand(view),
                new LoadCommand(view, boardRepository),
                new MoveCommand(view),
                new SaveCommand(view, boardRepository),
                new SaveGameCommand(view, gameXmlRepository),
                new LoadGameCommand(view, gameXmlRepository),
                new ShowCommand(view),
                new HighScoreCommand(view, highScoreRepository),
                new NewGameCommand(view),
                new QuitCommand()));
    }

    /**
     * @param phase az aktuális fázis
     * @return az ebben a fázisban használható parancsok
     */
    public List<Command> availableCommands(GamePhase phase) {
        return commands.stream()
                .filter(command -> command.isApplicableInPhase(phase))
                .toList();
    }

    /**
     * @param input a beírt sor
     * @param phase az aktuális fázis
     * @return az első parancs, amelyik az adott fázisban használható és illeszkedik a sorra
     */
    public Optional<MatchedCommand> findCommand(String input, GamePhase phase) {
        for (Command command : availableCommands(phase)) {
            Matcher matcher = command.match(input);
            if (matcher.matches()) {
                return Optional.of(new MatchedCommand(command, matcher));
            }
        }
        return Optional.empty();
    }

    /**
     * @param input a beírt sor
     * @return igaz, ha a sor valamelyik parancsra illeszkedik, fázistól függetlenül
     */
    public boolean matchesAnyCommand(String input) {
        return commands.stream().anyMatch(command -> command.match(input).matches());
    }

    /**
     * Egy megtalált parancs és a beírt sorra illesztett {@link Matcher}.
     *
     * @param command a parancs
     * @param matcher az illesztés eredménye (ebből olvashatók ki a paraméterek)
     */
    public record MatchedCommand(Command command, Matcher matcher) {
    }
}
