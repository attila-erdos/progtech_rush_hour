package hu.nye.progtech.command;

import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hu.nye.progtech.model.GamePhase;
import hu.nye.progtech.model.GameState;

/**
 * Egy parancs (Command tervezési minta). Minden parancs maga tudja:
 * <ul>
 *     <li>milyen szöveggel lehet kiadni (reguláris kifejezés),</li>
 *     <li>mely játékfázisokban használható,</li>
 *     <li>mit csinál a játék állapotával ({@link #apply(GameState, Matcher)}).</li>
 * </ul>
 */
public abstract class Command {

    private final String name;
    private final String syntax;
    private final Pattern pattern;
    private final Set<GamePhase> allowedPhases;

    /**
     * @param name a parancs neve
     * @param syntax a súgóban megjelenő formátum, pl. "move &lt;betű&gt; &lt;szám&gt;"
     * @param regex a parancsot felismerő reguláris kifejezés (kis- és nagybetű nem számít)
     * @param allowedPhases a fázisok, amelyekben a parancs használható
     */
    protected Command(String name, String syntax, String regex, Set<GamePhase> allowedPhases) {
        this.name = name;
        this.syntax = syntax;
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        this.allowedPhases = EnumSet.copyOf(allowedPhases);
    }

    /**
     * @param input a beírt sor
     * @return a beírt sorra illesztett {@link Matcher}; a {@code matches()} mondja meg, hogy illeszkedik-e
     */
    public Matcher match(String input) {
        return pattern.matcher(input.strip());
    }

    /**
     * @param phase az aktuális fázis
     * @return igaz, ha a parancs ebben a fázisban használható
     */
    public boolean isApplicableInPhase(GamePhase phase) {
        return allowedPhases.contains(phase);
    }

    /**
     * Végrehajtja a parancsot.
     *
     * @param state a játék állapota, ezt módosítja a parancs
     * @param matcher a beírt sorra illeszkedő {@link Matcher}, ebből olvashatók ki a paraméterek
     */
    public abstract void apply(GameState state, Matcher matcher);

    public String getName() {
        return name;
    }

    public String getSyntax() {
        return syntax;
    }
}
