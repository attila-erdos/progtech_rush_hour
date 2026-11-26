package hu.nye.progtech.persistence;

/**
 * Akkor dobódik, ha egy szöveges pályaleírás hibás.
 */
public class BoardFormatException extends Exception {

    private static final long serialVersionUID = 1L;

    public BoardFormatException(String message) {
        super(message);
    }
}
