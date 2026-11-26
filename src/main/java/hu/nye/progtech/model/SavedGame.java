package hu.nye.progtech.model;

import java.util.Objects;

/**
 * Egy félbehagyott játék mentett állapota (Value Object): a játékos neve és a tábla.
 *
 * @param playerName a játékos neve
 * @param board a tábla állása
 */
public record SavedGame(String playerName, Board board) {

    public SavedGame {
        Objects.requireNonNull(playerName, "playerName");
        Objects.requireNonNull(board, "board");
    }
}
