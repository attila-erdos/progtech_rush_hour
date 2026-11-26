package hu.nye.progtech.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import hu.nye.progtech.model.Board;
import hu.nye.progtech.model.Move;
import hu.nye.progtech.model.Orientation;
import hu.nye.progtech.model.Position;
import hu.nye.progtech.model.Vehicle;
import hu.nye.progtech.model.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BoardServiceTest {

    /*
     * A tesztekben használt 6x12-es tábla (0-tól indexelve):
     *
     *     0 1 2 3 4 5 6 7 8 9 10 11
     *  0  . . . . . . . . . . .  .
     *  1  . . . C . . . . . . .  .
     *  2  A A . C . . . . . . .  .
     *  3  . . . . . . . . . . .  .
     *  4  . . . . . . . B B . .  .
     *  5  . . . . . . . . . . .  .
     */
    private static final Vehicle HERO_A = horizontal('A', 2, 0, 2);
    private static final Vehicle HERO_B = horizontal('B', 4, 7, 2);
    private static final Vehicle CAR_C = vertical('C', 1, 3, 2);
    private static final Board BOARD = new Board(6, 12, List.of(HERO_A, HERO_B, CAR_C));

    private BoardService service;

    @BeforeEach
    void setUp() {
        service = new BoardService(BOARD);
    }

    private static Vehicle horizontal(char id, int row, int col, int length) {
        return new Vehicle(id, new Position(row, col), length, Orientation.HORIZONTAL);
    }

    private static Vehicle vertical(char id, int row, int col, int length) {
        return new Vehicle(id, new Position(row, col), length, Orientation.VERTICAL);
    }

    @Test
    void possibleMovesStopBeforeAnotherVehicleAndAtTheEdge() {
        // A jobbra csak 1-et mehet (C útban van), balra semmit (tábla széle)
        assertEquals(List.of(new Move('A', 1)), service.determinePossibleMoves(HERO_A));
    }

    @Test
    void possibleMovesAllowLongSlidesUpToTheEdge() {
        List<Move> moves = service.determinePossibleMoves(HERO_B);

        assertEquals(10, moves.size());
        assertTrue(moves.contains(new Move('B', 3)));
        assertFalse(moves.contains(new Move('B', 4)));
        assertTrue(moves.contains(new Move('B', -7)));
        assertFalse(moves.contains(new Move('B', -8)));
    }

    @Test
    void possibleMovesOfVerticalVehicleGoUpAndDown() {
        Set<Move> expected = Set.of(
                new Move('C', -1), new Move('C', 1), new Move('C', 2), new Move('C', 3));

        assertEquals(expected, new HashSet<>(service.determinePossibleMoves(CAR_C)));
    }

    @Test
    void possibleMovesOfWholeBoardContainEveryVehicle() {
        assertEquals(1 + 10 + 4, service.determinePossibleMoves().size());
    }

    @Test
    void isLegalChecksPathAndVehicle() {
        assertTrue(service.isLegal(new Move('C', 3)));
        assertFalse(service.isLegal(new Move('A', 2)));
        assertFalse(service.isLegal(new Move('X', 1)));
    }

    @Test
    void doMoveReplacesTheBoardWithANewOne() {
        service.doMove(new Move('C', -1));

        assertEquals(new Position(0, 3), service.getBoard().findVehicle('C').orElseThrow().start());
        assertEquals(new Position(1, 3), BOARD.findVehicle('C').orElseThrow().start());
    }

    @Test
    void doMoveRejectsBlockedPathAndUnknownVehicle() {
        assertThrows(IllegalMoveException.class, () -> service.doMove(new Move('A', 2)));
        assertThrows(IllegalMoveException.class, () -> service.doMove(new Move('X', 1)));
        assertEquals(BOARD, service.getBoard());
    }

    @Test
    void noWinnerAtStart() {
        assertEquals(Optional.empty(), service.winner());
    }

    @Test
    void heroAWinsAtRightEdge() {
        BoardService finished = new BoardService(new Board(6, 12, List.of(horizontal('A', 2, 10, 2), HERO_B)));

        assertEquals(Optional.of(VehicleType.HERO_A), finished.winner());
    }

    @Test
    void heroBWinsAtLeftEdge() {
        service.doMove(new Move('B', -7));

        assertEquals(Optional.of(VehicleType.HERO_B), service.winner());
    }

    @Test
    void neutralVehicleAtEdgeIsNotAWinner() {
        BoardService edges = new BoardService(new Board(6, 12, List.of(HERO_A, HERO_B, horizontal('D', 0, 10, 2))));

        assertEquals(Optional.empty(), edges.winner());
    }

    @Test
    void heroBAtRightEdgeIsNotAtExit() {
        Vehicle wrongSide = horizontal('B', 4, 10, 2);

        assertFalse(BoardService.isAtExit(new Board(6, 12, List.of(HERO_A, wrongSide)), wrongSide));
    }

    @Test
    void validStartingBoardIsAccepted() {
        assertDoesNotThrow(() -> BoardService.validateStartingBoard(BOARD));
    }

    @Test
    void invalidStartingBoardsAreRejected() {
        List<Board> invalid = List.of(
                new Board(6, 12, List.of(HERO_A, CAR_C)),
                new Board(6, 12, List.of(vertical('A', 0, 5, 2), HERO_B)),
                new Board(6, 12, List.of(horizontal('A', 2, 10, 2), HERO_B)),
                new Board(6, 12, List.of(HERO_A, horizontal('B', 4, 0, 2))));

        for (Board board : invalid) {
            assertThrows(IllegalArgumentException.class, () -> BoardService.validateStartingBoard(board));
        }
    }

    @Test
    void loadBoardReplacesBoardOnlyIfValid() {
        Board other = new Board(1, 6, List.of(horizontal('B', 0, 1, 2), horizontal('A', 0, 3, 2)));

        service.loadBoard(other);
        assertEquals(other, service.getBoard());

        assertThrows(IllegalArgumentException.class, () -> service.loadBoard(new Board(1, 6, List.of())));
        assertEquals(other, service.getBoard());
    }

    @Test
    void starterBoardIsAValidSixByTwelveBoard() {
        Board starter = BoardService.getStarterBoard();

        assertEquals(6, starter.rows());
        assertEquals(12, starter.cols());
        assertDoesNotThrow(() -> BoardService.validateStartingBoard(starter));
    }

    @Test
    void machinePicksAnotherVehicleWhenTheFirstOneIsBlocked() {
        // AADD. : az A be van szorulva, a D csak 1-et léphet jobbra
        Board board = new Board(1, 5, List.of(horizontal('A', 0, 0, 2), horizontal('D', 0, 2, 2)));
        Random random = mock(Random.class);
        when(random.nextInt(2)).thenReturn(0, 1);
        when(random.nextInt(1)).thenReturn(0);
        BoardService machine = new BoardService(board, random);

        assertEquals(Optional.of(new Move('D', 1)), machine.doARandomMachineMove());
        assertEquals(new Position(0, 3), machine.getBoard().findVehicle('D').orElseThrow().start());
        verify(random, times(2)).nextInt(2);
    }

    @Test
    void machineMoveIsEmptyWhenNothingCanMove() {
        Board board = new Board(1, 4, List.of(horizontal('A', 0, 0, 2), horizontal('D', 0, 2, 2)));

        assertEquals(Optional.empty(), new BoardService(board).doARandomMachineMove());
    }

    @Test
    void machineAlwaysMakesALegalMove() {
        BoardService machine = new BoardService(BoardService.getStarterBoard(), new Random(42));

        for (int i = 0; i < 100; i++) {
            Board before = machine.getBoard();
            Move move = machine.doARandomMachineMove().orElseThrow();
            assertTrue(new BoardService(before).isLegal(move));
        }
    }
}
