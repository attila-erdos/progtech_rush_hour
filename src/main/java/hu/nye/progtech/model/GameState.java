package hu.nye.progtech.model;

import java.util.Objects;
import java.util.Optional;

import hu.nye.progtech.service.BoardService;

/**
 * A futó program teljes, változó állapota: az aktuális fázis, a táblát kezelő szolgáltatás,
 * a játékos neve, a győztes, és hogy kérte-e a kilépést. A parancsok ezt módosítják.
 */
public class GameState {

    /** Az alapértelmezett játékosnév, ha nem ad meg nevet. */
    public static final String DEFAULT_PLAYER_NAME = "Játékos";
    /** A gépi játékos neve. */
    public static final String MACHINE_NAME = "Gép";

    private GamePhase phase = GamePhase.SETUP;
    private final BoardService boardService;
    private String playerName = DEFAULT_PLAYER_NAME;
    private String winnerName;
    private boolean exitRequested;

    /**
     * @param boardService a kezdőtáblát tartalmazó szolgáltatás
     */
    public GameState(BoardService boardService) {
        this.boardService = Objects.requireNonNull(boardService);
    }

    public GamePhase getPhase() {
        return phase;
    }

    public void setPhase(GamePhase phase) {
        this.phase = Objects.requireNonNull(phase);
    }

    public BoardService getBoardService() {
        return boardService;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = Objects.requireNonNull(playerName);
    }

    public Optional<String> getWinnerName() {
        return Optional.ofNullable(winnerName);
    }

    public void setWinnerName(String winnerName) {
        this.winnerName = winnerName;
    }

    public boolean isExitRequested() {
        return exitRequested;
    }

    public void setExitRequested(boolean exitRequested) {
        this.exitRequested = exitRequested;
    }
}
