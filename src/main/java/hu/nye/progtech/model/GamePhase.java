package hu.nye.progtech.model;

/**
 * A játék életciklusának fázisai. Minden parancs csak bizonyos fázisokban használható.
 *
 * <pre>
 * SETUP --start--&gt; PLAYING --győzelem--&gt; GAME_ENDED --new--&gt; SETUP
 * </pre>
 */
public enum GamePhase {
    /** Előkészítés: pálya betöltése, a játék még nem indult el. */
    SETUP,
    /** Folyamatban lévő játék: a játékos és a gép felváltva lép. */
    PLAYING,
    /** Valaki nyert, a játék véget ért. */
    GAME_ENDED
}
