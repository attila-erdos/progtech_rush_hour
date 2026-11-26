package hu.nye.progtech.model;

/**
 * Egy mező a táblán (Value Object). A sor és az oszlop is 0-tól indul.
 * A kiírásnál ebből lesz pl. "a1" (oszlop betűvel, sor 1-től).
 *
 * @param row sor indexe (0-tól)
 * @param col oszlop indexe (0-tól)
 */
public record Position(int row, int col) {

    /**
     * Visszaad egy új pozíciót, ami a megadott tengely mentén {@code steps} mezővel arrébb van.
     * Negatív {@code steps} balra / felfelé léptet. Az eredmény lehet a táblán kívül is.
     *
     * @param orientation melyik tengely mentén
     * @param steps hány mezőt (előjeles)
     * @return az új pozíció
     */
    public Position shift(Orientation orientation, int steps) {
        if (orientation == Orientation.HORIZONTAL) {
            return new Position(row, col + steps);
        }
        return new Position(row + steps, col);
    }
}
