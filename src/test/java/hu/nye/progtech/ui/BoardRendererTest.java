package hu.nye.progtech.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import hu.nye.progtech.model.Board;
import hu.nye.progtech.model.Orientation;
import hu.nye.progtech.model.Position;
import hu.nye.progtech.model.Vehicle;
import org.junit.jupiter.api.Test;

class BoardRendererTest {

    private final BoardRenderer renderer = new BoardRenderer();

    @Test
    void rendersCoordinatesVehiclesAndExits() {
        Board board = new Board(3, 4, List.of(
                new Vehicle('A', new Position(0, 0), 2, Orientation.HORIZONTAL),
                new Vehicle('C', new Position(0, 3), 2, Orientation.VERTICAL),
                new Vehicle('B', new Position(2, 1), 2, Orientation.HORIZONTAL)));
        String nl = System.lineSeparator();

        String expected = "     a b c d " + nl
                + "   1 A A . C >" + nl
                + "   2 . . . C " + nl
                + "<  3 . B B . " + nl;

        assertEquals(expected, renderer.render(board));
    }

    @Test
    void boardWithoutHeroesHasNoExitMarks() {
        Board board = new Board(1, 2, List.of());
        String nl = System.lineSeparator();

        assertEquals("     a b " + nl + "   1 . . " + nl, renderer.render(board));
    }
}
