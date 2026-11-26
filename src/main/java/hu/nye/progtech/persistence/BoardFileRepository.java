package hu.nye.progtech.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import hu.nye.progtech.model.Board;

/**
 * Pálya betöltése szövegfájlból és mentése szövegfájlba ({@link BoardTextFormat} formátumban).
 */
public class BoardFileRepository {

    private final BoardTextFormat format;

    public BoardFileRepository(BoardTextFormat format) {
        this.format = format;
    }

    /**
     * @param path a fájl útvonala
     * @return a beolvasott tábla
     * @throws IOException ha a fájl nem olvasható (pl. nem létezik)
     * @throws BoardFormatException ha a fájl tartalma hibás
     */
    public Board load(Path path) throws IOException, BoardFormatException {
        return format.parse(Files.readAllLines(path, StandardCharsets.UTF_8));
    }

    /**
     * @param board a mentendő tábla
     * @param path a fájl útvonala (ha létezik, felülíródik)
     * @throws IOException ha a fájl nem írható
     */
    public void save(Board board, Path path) throws IOException {
        Files.write(path, format.format(board), StandardCharsets.UTF_8);
    }
}
