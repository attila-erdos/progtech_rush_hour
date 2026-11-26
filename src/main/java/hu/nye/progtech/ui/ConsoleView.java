package hu.nye.progtech.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;

import hu.nye.progtech.model.Board;

/**
 * A konzolos nézet (az MVC-ben a view): szöveget és táblát ír ki, sorokat olvas be.
 * A tesztekben a valódi konzol helyett más be- és kimeneti folyam adható át.
 */
public class ConsoleView {

    private final BufferedReader reader;
    private final PrintStream out;
    private final BoardRenderer renderer;

    /**
     * @param in bemenet (pl. {@code System.in})
     * @param out kimenet (pl. {@code System.out})
     * @param renderer a tábla szöveges kirajzolója
     */
    public ConsoleView(InputStream in, PrintStream out, BoardRenderer renderer) {
        this.reader = new BufferedReader(new InputStreamReader(in, Charset.defaultCharset()));
        this.out = out;
        this.renderer = renderer;
    }

    /**
     * @return a beolvasott sor, vagy {@code null}, ha vége a bemenetnek
     */
    public String readLine() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * @param text a kiírandó szöveg, sortöréssel
     */
    public void println(String text) {
        out.println(text);
    }

    /**
     * @param text a kiírandó szöveg, sortörés nélkül
     */
    public void print(String text) {
        out.print(text);
        out.flush();
    }

    /**
     * @param board a kirajzolandó tábla
     */
    public void showBoard(Board board) {
        out.println(renderer.render(board));
    }
}
