package hu.nye.progtech.persistence;

/**
 * Akkor dobódik, ha az adatbázis-művelet nem sikerült.
 */
public class DatabaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
