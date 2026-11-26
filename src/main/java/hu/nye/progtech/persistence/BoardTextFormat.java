package hu.nye.progtech.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import hu.nye.progtech.model.Board;
import hu.nye.progtech.model.Orientation;
import hu.nye.progtech.model.Position;
import hu.nye.progtech.model.Vehicle;

/**
 * A pálya szöveges formátuma. Az első sor a méret ("sorok oszlopok"), utána soronként a mezők:
 * <pre>
 * 6 12
 * CCC...D.....
 * ....E.D..FF.
 * AA..E.D.G...
 * ...
 * </pre>
 * A '.' üres mező, egy nagybetű egy jármű része. Az azonos betűk egy járművet alkotnak,
 * ezeknek egy egyenes, folytonos vonalat kell kiadniuk.
 */
public class BoardTextFormat {

    private static final char EMPTY = '.';

    /**
     * Szövegsorokból táblát készít.
     *
     * @param lines a fájl sorai
     * @return a beolvasott tábla
     * @throws BoardFormatException ha a szöveg nem érvényes pályaleírás
     */
    public Board parse(List<String> lines) throws BoardFormatException {
        List<String> content = lines.stream()
                .map(String::strip)
                .filter(line -> !line.isEmpty())
                .toList();
        if (content.isEmpty()) {
            throw new BoardFormatException("Üres pályaleírás.");
        }
        int[] size = parseSize(content.get(0));
        int rows = size[0];
        int cols = size[1];
        if (content.size() - 1 != rows) {
            throw new BoardFormatException("A tábla " + rows + " soros, de "
                    + (content.size() - 1) + " sor van megadva.");
        }

        Map<Character, List<Position>> cellsById = new TreeMap<>();
        for (int row = 0; row < rows; row++) {
            String line = content.get(row + 1);
            if (line.length() != cols) {
                throw new BoardFormatException("A(z) " + (row + 1) + ". sor hossza nem " + cols + ".");
            }
            for (int col = 0; col < cols; col++) {
                char c = line.charAt(col);
                if (c == EMPTY) {
                    continue;
                }
                if (c < 'A' || c > 'Z') {
                    throw new BoardFormatException("Érvénytelen karakter: '" + c + "'");
                }
                cellsById.computeIfAbsent(c, key -> new ArrayList<>()).add(new Position(row, col));
            }
        }

        List<Vehicle> vehicles = new ArrayList<>();
        for (Map.Entry<Character, List<Position>> entry : cellsById.entrySet()) {
            vehicles.add(toVehicle(entry.getKey(), entry.getValue()));
        }
        return new Board(rows, cols, vehicles);
    }

    private int[] parseSize(String header) throws BoardFormatException {
        String[] parts = header.split("\\s+");
        if (parts.length != 2) {
            throw new BoardFormatException("Az első sor formátuma: \"sorok oszlopok\", pl. \"6 12\".");
        }
        try {
            int rows = Integer.parseInt(parts[0]);
            int cols = Integer.parseInt(parts[1]);
            if (rows < 1 || cols < 1) {
                throw new BoardFormatException("A tábla mérete pozitív kell legyen.");
            }
            return new int[] {rows, cols};
        } catch (NumberFormatException e) {
            throw new BoardFormatException("A tábla mérete nem szám: " + header);
        }
    }

    /**
     * Az egy betűhöz tartozó mezőkből járművet készít. A mezők sor-oszlop sorrendben érkeznek,
     * így az első mező mindig a jármű bal felső mezője.
     */
    private Vehicle toVehicle(char id, List<Position> cells) throws BoardFormatException {
        if (cells.size() < Vehicle.MIN_LENGTH) {
            throw new BoardFormatException("A(z) " + id + " jármű túl rövid.");
        }
        Position start = cells.get(0);
        Orientation orientation = start.row() == cells.get(1).row() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
        for (int i = 0; i < cells.size(); i++) {
            if (!cells.get(i).equals(start.shift(orientation, i))) {
                throw new BoardFormatException("A(z) " + id + " jármű nem egyenes, folytonos vonal.");
            }
        }
        return new Vehicle(id, start, cells.size(), orientation);
    }

    /**
     * Táblából szövegsorokat készít (a {@link #parse(List)} fordítottja).
     *
     * @param board a tábla
     * @return a szöveges leírás sorai
     */
    public List<String> format(Board board) {
        char[][] grid = new char[board.rows()][board.cols()];
        for (char[] row : grid) {
            Arrays.fill(row, EMPTY);
        }
        for (Vehicle vehicle : board.vehicles()) {
            for (Position cell : vehicle.cells()) {
                grid[cell.row()][cell.col()] = vehicle.id();
            }
        }
        List<String> lines = new ArrayList<>();
        lines.add(board.rows() + " " + board.cols());
        for (char[] row : grid) {
            lines.add(new String(row));
        }
        return lines;
    }
}
