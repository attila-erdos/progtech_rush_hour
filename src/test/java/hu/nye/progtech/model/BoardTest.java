package hu.nye.progtech.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class BoardTest {

    private static final Vehicle CAR_A = new Vehicle('A', new Position(0, 0), 2, Orientation.HORIZONTAL);
    private static final Vehicle CAR_D = new Vehicle('D', new Position(1, 2), 3, Orientation.VERTICAL);

    @Test
    void overlappingVehiclesAreRejected() {
        Vehicle overlapping = new Vehicle('E', new Position(0, 1), 2, Orientation.VERTICAL);

        assertThrows(IllegalArgumentException.class, () -> new Board(6, 6, List.of(CAR_A, overlapping)));
    }

    @Test
    void vehicleOutsideTheBoardIsRejected() {
        Vehicle outside = new Vehicle('E', new Position(0, 5), 2, Orientation.HORIZONTAL);

        assertThrows(IllegalArgumentException.class, () -> new Board(6, 6, List.of(outside)));
    }

    @Test
    void duplicateIdsAreRejected() {
        Vehicle sameId = new Vehicle('A', new Position(5, 0), 2, Orientation.HORIZONTAL);

        assertThrows(IllegalArgumentException.class, () -> new Board(6, 6, List.of(CAR_A, sameId)));
    }

    @Test
    void nonPositiveSizeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Board(0, 6, List.of()));
    }

    @Test
    void boardKeepsItsOwnCopyOfVehicles() {
        List<Vehicle> list = new ArrayList<>(List.of(CAR_A));
        Board board = new Board(6, 6, list);

        list.add(CAR_D);

        assertEquals(1, board.vehicles().size());
    }

    @Test
    void vehicleAtAndIsFree() {
        Board board = new Board(6, 6, List.of(CAR_A, CAR_D));

        assertEquals(Optional.of(CAR_D), board.vehicleAt(new Position(3, 2)));
        assertFalse(board.isFree(new Position(0, 1)));
        assertTrue(board.isFree(new Position(5, 5)));
        assertFalse(board.isFree(new Position(6, 0)));
    }

    @Test
    void withVehicleReplacesOnlyThatVehicle() {
        Board board = new Board(6, 6, List.of(CAR_A, CAR_D));

        Board moved = board.withVehicle(CAR_A.moved(3));

        assertEquals(new Position(0, 3), moved.findVehicle('A').orElseThrow().start());
        assertEquals(CAR_D, moved.findVehicle('D').orElseThrow());
    }

    @Test
    void withVehicleRejectsUnknownVehicle() {
        Board board = new Board(6, 6, List.of(CAR_A));

        assertThrows(IllegalArgumentException.class, () -> board.withVehicle(CAR_D));
    }

    @Test
    void vehicleTypeComesFromId() {
        assertEquals(VehicleType.HERO_A, CAR_A.type());
        assertEquals(VehicleType.NEUTRAL, CAR_D.type());
        assertEquals(VehicleType.HERO_B, VehicleType.fromId('B'));
    }

    @Test
    void tooShortVehicleAndZeroMoveAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Vehicle('E', new Position(0, 0), 1, Orientation.HORIZONTAL));
        assertThrows(IllegalArgumentException.class, () -> new Move('A', 0));
    }
}
