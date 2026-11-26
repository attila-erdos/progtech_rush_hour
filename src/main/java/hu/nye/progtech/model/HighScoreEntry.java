package hu.nye.progtech.model;

/**
 * Egy sor a dicsőséglistán (Value Object).
 *
 * @param playerName a játékos neve
 * @param wins a győzelmek száma
 */
public record HighScoreEntry(String playerName, int wins) {
}
