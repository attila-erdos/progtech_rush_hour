package hu.nye.progtech.service;

/**
 * Akkor dobódik, ha egy lépés nem hajtható végre (nincs ilyen jármű, vagy nem szabad az út).
 */
public class IllegalMoveException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public IllegalMoveException(String message) {
        super(message);
    }
}
