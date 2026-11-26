package hu.nye.progtech.model;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Egy jármű a táblán (immutable Value Object).
 * A {@code start} mindig a bal felső mezője (vízszintesnél a bal szélső, függőlegesnél a legfelső).
 *
 * @param id a jármű betűjele ('A' és 'B' a céljárművek)
 * @param start a jármű bal felső mezője
 * @param length a jármű hossza mezőkben (legalább 2)
 * @param orientation a jármű tengelye
 */
public record Vehicle(char id, Position start, int length, Orientation orientation) {

    /** Egy jármű legkisebb megengedett hossza. */
    public static final int MIN_LENGTH = 2;

    public Vehicle {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(orientation, "orientation");
        if (length < MIN_LENGTH) {
            throw new IllegalArgumentException("A jármű hossza legalább " + MIN_LENGTH + " kell legyen: " + id);
        }
    }

    /**
     * @return a jármű szerepe (céljármű vagy semleges)
     */
    public VehicleType type() {
        return VehicleType.fromId(id);
    }

    /**
     * @return a jármű jobb alsó mezője (vízszintesnél a jobb szélső, függőlegesnél a legalsó)
     */
    public Position end() {
        return start.shift(orientation, length - 1);
    }

    /**
     * @return a jármű által elfoglalt összes mező
     */
    public List<Position> cells() {
        return IntStream.range(0, length)
                .mapToObj(i -> start.shift(orientation, i))
                .toList();
    }

    /**
     * @param position a vizsgált mező
     * @return igaz, ha a jármű elfoglalja ezt a mezőt
     */
    public boolean occupies(Position position) {
        return cells().contains(position);
    }

    /**
     * Az eredeti jármű nem változik, egy új, elmozdított példányt ad vissza.
     *
     * @param steps hány mezőt mozduljon (előjeles)
     * @return az elmozdított jármű
     */
    public Vehicle moved(int steps) {
        return new Vehicle(id, start.shift(orientation, steps), length, orientation);
    }
}
