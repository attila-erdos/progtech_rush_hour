package hu.nye.progtech.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A játéktábla (immutable Value Object): mérete és a rajta lévő járművek.
 * Létrehozáskor ellenőrzi, hogy minden jármű a táblán van és nincs átfedés.
 * Lépéskor nem módosul, hanem új {@code Board} jön létre.
 *
 * @param rows sorok száma (N)
 * @param cols oszlopok száma (M)
 * @param vehicles a járművek (a lista másolatát tárolja, nem módosítható)
 */
public record Board(int rows, int cols, List<Vehicle> vehicles) {

    public Board {
        if (rows < 1 || cols < 1) {
            throw new IllegalArgumentException("A tábla mérete pozitív kell legyen: " + rows + "x" + cols);
        }
        vehicles = List.copyOf(vehicles);
        validateVehicles(rows, cols, vehicles);
    }

    private static void validateVehicles(int rows, int cols, List<Vehicle> vehicles) {
        Set<Character> ids = new HashSet<>();
        Set<Position> occupied = new HashSet<>();
        for (Vehicle vehicle : vehicles) {
            if (!ids.add(vehicle.id())) {
                throw new IllegalArgumentException("Két jármű azonos betűjellel: " + vehicle.id());
            }
            for (Position cell : vehicle.cells()) {
                if (!isInside(rows, cols, cell)) {
                    throw new IllegalArgumentException("A(z) " + vehicle.id() + " jármű lelóg a tábláról.");
                }
                if (!occupied.add(cell)) {
                    throw new IllegalArgumentException("A(z) " + vehicle.id()
                            + " jármű átfedésben van egy másikkal.");
                }
            }
        }
    }

    private static boolean isInside(int rows, int cols, Position position) {
        return position.row() >= 0 && position.row() < rows
                && position.col() >= 0 && position.col() < cols;
    }

    /**
     * @param position a vizsgált mező
     * @return igaz, ha a mező a táblán belül van
     */
    public boolean isInside(Position position) {
        return isInside(rows, cols, position);
    }

    /**
     * @param position a vizsgált mező
     * @return az ott álló jármű, vagy üres, ha a mező szabad
     */
    public Optional<Vehicle> vehicleAt(Position position) {
        return vehicles.stream()
                .filter(vehicle -> vehicle.occupies(position))
                .findFirst();
    }

    /**
     * @param position a vizsgált mező
     * @return igaz, ha a mező a táblán van és nem áll rajta jármű
     */
    public boolean isFree(Position position) {
        return isInside(position) && vehicleAt(position).isEmpty();
    }

    /**
     * @param id a keresett jármű betűjele
     * @return a jármű, vagy üres, ha nincs ilyen
     */
    public Optional<Vehicle> findVehicle(char id) {
        return vehicles.stream()
                .filter(vehicle -> vehicle.id() == id)
                .findFirst();
    }

    /**
     * Új táblát ad vissza, amin az azonos betűjelű jármű le van cserélve a megadottra.
     *
     * @param updated az új állapotú jármű
     * @return az új tábla
     * @throws IllegalArgumentException ha nincs ilyen betűjelű jármű, vagy az új helyzet érvénytelen
     */
    public Board withVehicle(Vehicle updated) {
        if (findVehicle(updated.id()).isEmpty()) {
            throw new IllegalArgumentException("Nincs ilyen jármű: " + updated.id());
        }
        List<Vehicle> newVehicles = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            newVehicles.add(vehicle.id() == updated.id() ? updated : vehicle);
        }
        return new Board(rows, cols, newVehicles);
    }
}
