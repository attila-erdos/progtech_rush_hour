package hu.nye.progtech.model;

/**
 * Egy lépés (Value Object): melyik járművet hány mezővel csúsztatjuk el.
 * Pozitív {@code steps} jobbra / lefelé, negatív balra / felfelé mozgat.
 *
 * @param vehicleId a mozgatott jármű betűjele
 * @param steps hány mező (előjeles, nem lehet 0)
 */
public record Move(char vehicleId, int steps) {

    public Move {
        if (steps == 0) {
            throw new IllegalArgumentException("A lépés nem lehet 0 mező.");
        }
    }
}
