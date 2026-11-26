package hu.nye.progtech.ui;

import hu.nye.progtech.model.Board;
import hu.nye.progtech.model.Position;
import hu.nye.progtech.model.Vehicle;
import hu.nye.progtech.model.VehicleType;

/**
 * A tábla szöveges megjelenítése a konzolon. Az oszlopok betűvel (a, b, c, ...), a sorok 1-től számozva.
 * A kijáratokat nyíl jelöli: "&gt;" az A céljármű sorának jobb végén,
 * "&lt;" a B céljármű sorának bal elején.
 * <pre>
 *      a b c d e f g h i j k l
 *    1 C C C . . . D . . . . .
 *    2 . . . . E . D . . F F .
 *    3 A A . . E . D . G . . . &gt;
 *  &lt; 4 . . H . . . . . G . B B
 * </pre>
 */
public class BoardRenderer {

    private static final char EMPTY = '.';

    /**
     * @param board a tábla
     * @return a tábla többsoros szöveges képe
     */
    public String render(Board board) {
        int exitRowA = heroRow(board, VehicleType.HERO_A_ID);
        int exitRowB = heroRow(board, VehicleType.HERO_B_ID);

        StringBuilder sb = new StringBuilder("     ");
        for (int col = 0; col < board.cols(); col++) {
            sb.append(columnLetter(col)).append(' ');
        }
        sb.append(System.lineSeparator());

        for (int row = 0; row < board.rows(); row++) {
            sb.append(row == exitRowB ? '<' : ' ');
            sb.append(String.format("%3d ", row + 1));
            for (int col = 0; col < board.cols(); col++) {
                sb.append(cellChar(board, new Position(row, col))).append(' ');
            }
            if (row == exitRowA) {
                sb.append('>');
            }
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    /**
     * @param col oszlopindex (0-tól)
     * @return az oszlop betűjele ('a', 'b', ...)
     */
    public static char columnLetter(int col) {
        return (char) ('a' + col);
    }

    private char cellChar(Board board, Position position) {
        return board.vehicleAt(position).map(Vehicle::id).orElse(EMPTY);
    }

    private int heroRow(Board board, char heroId) {
        return board.findVehicle(heroId).map(hero -> hero.start().row()).orElse(-1);
    }
}
