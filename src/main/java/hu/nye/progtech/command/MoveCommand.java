package hu.nye.progtech.command;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;

import hu.nye.progtech.model.GamePhase;
import hu.nye.progtech.model.GameState;
import hu.nye.progtech.model.Move;
import hu.nye.progtech.service.BoardService;
import hu.nye.progtech.service.IllegalMoveException;
import hu.nye.progtech.ui.ConsoleView;

/**
 * A humán játékos lépése, pl. "move C 2" (2 mezőt jobbra / lefelé) vagy "move C -1" (balra / felfelé).
 * Ha a lépés szabályos és még nincs győztes, utána automatikusan lép a gép is.
 */
public class MoveCommand extends Command {

    private final ConsoleView view;

    public MoveCommand(ConsoleView view) {
        super("move", "move <betű> <szám>", "move\\s+([a-z])\\s+(-?\\d{1,3})", Set.of(GamePhase.PLAYING));
        this.view = view;
    }

    @Override
    public void apply(GameState state, Matcher matcher) {
        char vehicleId = Character.toUpperCase(matcher.group(1).charAt(0));
        int steps = Integer.parseInt(matcher.group(2));
        if (steps == 0) {
            view.println("A lépés nem lehet 0 mező.");
            return;
        }
        BoardService boardService = state.getBoardService();
        try {
            boardService.doMove(new Move(vehicleId, steps));
        } catch (IllegalMoveException e) {
            view.println(e.getMessage());
            return;
        }
        view.showBoard(boardService.getBoard());

        if (boardService.winner().isEmpty()) {
            doMachineMove(boardService);
        }
    }

    private void doMachineMove(BoardService boardService) {
        Optional<Move> machineMove = boardService.doARandomMachineMove();
        if (machineMove.isEmpty()) {
            view.println("A gép nem tud lépni, kimarad.");
            return;
        }
        Move move = machineMove.get();
        view.println("A gép lépése: move " + move.vehicleId() + " " + move.steps());
        view.showBoard(boardService.getBoard());
    }
}
