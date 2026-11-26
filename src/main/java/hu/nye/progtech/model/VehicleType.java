package hu.nye.progtech.model;

/**
 * A jármű szerepe a játékban.
 */
public enum VehicleType {
    /** A humán játékos céljárműve, a jobb szélen kell kijutnia. */
    HERO_A,
    /** A gépi játékos céljárműve, a bal szélen kell kijutnia. */
    HERO_B,
    /** Semleges akadály. */
    NEUTRAL;

    /** A humán játékos céljárművének betűjele. */
    public static final char HERO_A_ID = 'A';
    /** A gépi játékos céljárművének betűjele. */
    public static final char HERO_B_ID = 'B';

    /**
     * A jármű betűjeléből megállapítja a szerepét.
     *
     * @param id a jármű betűjele
     * @return {@link #HERO_A} 'A' esetén, {@link #HERO_B} 'B' esetén, egyébként {@link #NEUTRAL}
     */
    public static VehicleType fromId(char id) {
        if (id == HERO_A_ID) {
            return HERO_A;
        }
        if (id == HERO_B_ID) {
            return HERO_B;
        }
        return NEUTRAL;
    }
}
