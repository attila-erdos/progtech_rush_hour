package hu.nye.progtech.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import hu.nye.progtech.model.Board;
import hu.nye.progtech.model.Orientation;
import hu.nye.progtech.model.Position;
import hu.nye.progtech.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BoardTextFormatTest {

    private static final List<String> SAMPLE = List.of(
            "3 5",
            "AA..C",
            "....C",
            "..BB."
    );

    private BoardTextFormat format;

    @BeforeEach
    void setUp() {
        format = new BoardTextFormat();
    }

    @Test
    void parseRecognizesHorizontalAndVerticalVehicles() throws BoardFormatException {
        Board board = format.parse(SAMPLE);

        assertEquals(3, board.rows());
        assertEquals(5, board.cols());
        assertEquals(new Vehicle('A', new Position(0, 0), 2, Orientation.HORIZONTAL),
                board.findVehicle('A').orElseThrow());
        assertEquals(new Vehicle('C', new Position(0, 4), 2, Orientation.VERTICAL),
                board.findVehicle('C').orElseThrow());
        assertEquals(3, board.vehicles().size());
    }

    @Test
    void parseIgnoresEmptyLinesAndSurroundingSpaces() throws BoardFormatException {
        Board board = format.parse(List.of("", " 1 3 ", "AA.", ""));

        assertEquals(1, board.vehicles().size());
    }

    @Test
    void formatIsTheInverseOfParse() throws BoardFormatException {
        assertEquals(SAMPLE, format.format(format.parse(SAMPLE)));
    }

    @Test
    void emptyInputIsRejected() {
        assertThrows(BoardFormatException.class, () -> format.parse(List.of()));
    }

    @Test
    void badHeaderIsRejected() {
        assertThrows(BoardFormatException.class, () -> format.parse(List.of("3", "AA.")));
        assertThrows(BoardFormatException.class, () -> format.parse(List.of("x y", "AA.")));
        assertThrows(BoardFormatException.class, () -> format.parse(List.of("0 3")));
    }

    @Test
    void wrongNumberOfRowsIsRejected() {
        assertThrows(BoardFormatException.class, () -> format.parse(List.of("2 3", "AA.")));
    }

    @Test
    void wrongRowLengthIsRejected() {
        assertThrows(BoardFormatException.class, () -> format.parse(List.of("1 3", "AA..")));
    }

    @Test
    void invalidCharacterIsRejected() {
        assertThrows(BoardFormatException.class, () -> format.parse(List.of("1 3", "aa.")));
    }

    @Test
    void singleCellVehicleIsRejected() {
        assertThrows(BoardFormatException.class, () -> format.parse(List.of("1 3", "A..")));
    }

    @Test
    void bentVehicleIsRejected() {
        assertThrows(BoardFormatException.class, () -> format.parse(List.of("2 3", "AA.", ".A.")));
    }

    @Test
    void brokenVehicleIsRejected() {
        assertThrows(BoardFormatException.class, () -> format.parse(List.of("1 4", "A.A.")));
    }
}
