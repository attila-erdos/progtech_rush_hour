package hu.nye.progtech.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import hu.nye.progtech.model.Board;
import hu.nye.progtech.model.Move;
import hu.nye.progtech.model.Orientation;
import hu.nye.progtech.model.Position;
import hu.nye.progtech.model.Vehicle;
import hu.nye.progtech.model.VehicleType;
import hu.nye.progtech.persistence.BoardFormatException;
import hu.nye.progtech.persistence.BoardTextFormat;

/**
 * A játékszabályok és az aktuális tábla kezelése.
 *
 * <ul>
 *     <li>Bármelyik jármű mozgatható, csak a saját tengelye mentén.</li>
 *     <li>Egy lépésben bármennyi szabad mezőt csúszhat, de nem ugorhat át semmit,
 *         és nem hagyhatja el a táblát.</li>
 *     <li>Az "A" céljármű a jobb szélen, a "B" a bal szélen jut ki. Akié a kijutott céljármű, az nyer
 *         (akkor is, ha az ellenfél tolta ki).</li>
 * </ul>
 *
 * A {@link Board} immutable: minden lépés egy új táblát hoz létre, és ez lecseréli a régit.
 */
public class BoardService {

    /** A beégetett alapértelmezett 6x12-es pálya szöveges leírása. */
    public static final List<String> STARTER_BOARD_LINES = List.of(
            "6 12",
            "CCC...D.....",
            "....E.D..FF.",
            "AA..E.D.G...",
            "..H.....G.BB",
            "..H.II....J.",
            "KK........J."
    );

    private static final int FORWARD = 1;
    private static final int BACKWARD = -1;

    private Board board;
    private final Random random;

    /**
     * @param board a kezdő tábla
     */
    public BoardService(Board board) {
        this(board, new Random());
    }

    /**
     * @param board a kezdő tábla
     * @param random a gépi lépésekhez használt véletlenszám-generátor (tesztben kicserélhető)
     */
    public BoardService(Board board, Random random) {
        this.board = board;
        this.random = random;
    }

    public Board getBoard() {
        return board;
    }

    /**
     * Új kezdőállást tölt be (pl. fájlból vagy új játéknál).
     *
     * @param newBoard az új tábla
     * @throws IllegalArgumentException ha a tábla nem érvényes kezdőállás
     */
    public void loadBoard(Board newBoard) {
        validateStartingBoard(newBoard);
        this.board = newBoard;
    }

    /**
     * Egy jármű összes érvényes lépése.
     *
     * @param vehicle a vizsgált jármű
     * @return a lehetséges lépések (üres, ha a jármű be van szorulva)
     */
    public List<Move> determinePossibleMoves(Vehicle vehicle) {
        List<Move> moves = new ArrayList<>();
        addMovesInDirection(vehicle, FORWARD, moves);
        addMovesInDirection(vehicle, BACKWARD, moves);
        return moves;
    }

    /**
     * Az adott irányban addig lépked, amíg szabad mezőt talál; minden szabad mező egy lehetséges lépés.
     */
    private void addMovesInDirection(Vehicle vehicle, int direction, List<Move> moves) {
        Position front = direction == FORWARD ? vehicle.end() : vehicle.start();
        int steps = 1;
        while (board.isFree(front.shift(vehicle.orientation(), direction * steps))) {
            moves.add(new Move(vehicle.id(), direction * steps));
            steps++;
        }
    }

    /**
     * @return a táblán lehetséges összes lépés
     */
    public List<Move> determinePossibleMoves() {
        return board.vehicles().stream()
                .flatMap(vehicle -> determinePossibleMoves(vehicle).stream())
                .toList();
    }

    /**
     * @param move a vizsgált lépés
     * @return igaz, ha a lépés végrehajtható
     */
    public boolean isLegal(Move move) {
        return board.findVehicle(move.vehicleId())
                .map(vehicle -> determinePossibleMoves(vehicle).contains(move))
                .orElse(false);
    }

    /**
     * Végrehajt egy lépést: egy új táblát hoz létre, és az lesz az aktuális.
     *
     * @param move a lépés
     * @throws IllegalMoveException ha nincs ilyen jármű, vagy nem szabad az út
     */
    public void doMove(Move move) {
        Vehicle vehicle = board.findVehicle(move.vehicleId())
                .orElseThrow(() -> new IllegalMoveException("Nincs ilyen jármű: " + move.vehicleId()));
        if (!determinePossibleMoves(vehicle).contains(move)) {
            throw new IllegalMoveException("A(z) " + move.vehicleId() + " jármű nem mozdulhat "
                    + move.steps() + " mezőt, mert nem szabad az út.");
        }
        board = board.withVehicle(vehicle.moved(move.steps()));
    }

    /**
     * A gépi játékos lépése: véletlenszerűen választ egy járművet, és ha annak van lehetséges lépése,
     * abból is véletlenszerűen választ egyet és végrehajtja. Ha nincs, újra választ járművet.
     *
     * @return a végrehajtott lépés, vagy üres, ha egyik jármű sem tud lépni
     */
    public Optional<Move> doARandomMachineMove() {
        if (determinePossibleMoves().isEmpty()) {
            return Optional.empty();
        }
        List<Vehicle> vehicles = board.vehicles();
        while (true) {
            Vehicle vehicle = vehicles.get(random.nextInt(vehicles.size()));
            List<Move> moves = determinePossibleMoves(vehicle);
            if (!moves.isEmpty()) {
                Move move = moves.get(random.nextInt(moves.size()));
                doMove(move);
                return Optional.of(move);
            }
        }
    }

    /**
     * @return a kijutott céljármű típusa (ennek a tulajdonosa nyert), vagy üres, ha még nincs győztes
     */
    public Optional<VehicleType> winner() {
        return board.vehicles().stream()
                .filter(vehicle -> isAtExit(board, vehicle))
                .map(Vehicle::type)
                .findFirst();
    }

    /**
     * @param board a tábla
     * @param vehicle a vizsgált jármű
     * @return igaz, ha ez egy céljármű, és elérte a saját kijáratát
     */
    public static boolean isAtExit(Board board, Vehicle vehicle) {
        return switch (vehicle.type()) {
            case HERO_A -> vehicle.end().col() == board.cols() - 1;
            case HERO_B -> vehicle.start().col() == 0;
            default -> false;
        };
    }

    /**
     * Ellenőrzi, hogy a kezdőállás játszható-e: mindkét céljármű megvan, vízszintes,
     * és egyik sem áll már eleve a kijáratánál.
     *
     * @param board a kezdő tábla
     * @throws IllegalArgumentException ha a kezdőállás nem megfelelő
     */
    public static void validateStartingBoard(Board board) {
        validateHero(board, VehicleType.HERO_A_ID);
        validateHero(board, VehicleType.HERO_B_ID);
    }

    private static void validateHero(Board board, char heroId) {
        Vehicle hero = board.findVehicle(heroId)
                .orElseThrow(() -> new IllegalArgumentException("Hiányzik a(z) " + heroId + " céljármű."));
        if (hero.orientation() != Orientation.HORIZONTAL) {
            throw new IllegalArgumentException("A(z) " + heroId + " céljárműnek vízszintesnek kell lennie.");
        }
        if (isAtExit(board, hero)) {
            throw new IllegalArgumentException("A(z) " + heroId + " céljármű nem kezdhet a kijáratánál.");
        }
    }

    /**
     * @return a beégetett alapértelmezett 6x12-es pálya
     */
    public static Board getStarterBoard() {
        try {
            return new BoardTextFormat().parse(STARTER_BOARD_LINES);
        } catch (BoardFormatException e) {
            throw new IllegalStateException("A beégetett alap pálya hibás.", e);
        }
    }
}
