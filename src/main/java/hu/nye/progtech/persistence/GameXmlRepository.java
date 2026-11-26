package hu.nye.progtech.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import hu.nye.progtech.model.Board;
import hu.nye.progtech.model.Orientation;
import hu.nye.progtech.model.Position;
import hu.nye.progtech.model.SavedGame;
import hu.nye.progtech.model.Vehicle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Folyamatban lévő játék mentése XML fájlba és visszatöltése. A JDK beépített DOM API-ját használja:
 * <pre>
 * &lt;game&gt;
 *   &lt;player&gt;Attila&lt;/player&gt;
 *   &lt;board rows="6" cols="12"&gt;
 *     &lt;vehicle id="A" row="2" col="0" length="2" orientation="HORIZONTAL"/&gt;
 *     ...
 *   &lt;/board&gt;
 * &lt;/game&gt;
 * </pre>
 */
public class GameXmlRepository {

    private static final String GAME = "game";
    private static final String PLAYER = "player";
    private static final String BOARD = "board";
    private static final String VEHICLE = "vehicle";

    /**
     * @param game a mentendő játék
     * @param path a fájl útvonala (ha létezik, felülíródik)
     * @throws IOException ha a fájl nem írható
     */
    public void save(SavedGame game, Path path) throws IOException {
        try {
            Document document = newDocumentBuilder().newDocument();
            Element root = document.createElement(GAME);
            document.appendChild(root);

            Element player = document.createElement(PLAYER);
            player.setTextContent(game.playerName());
            root.appendChild(player);

            Board board = game.board();
            Element boardElement = document.createElement(BOARD);
            boardElement.setAttribute("rows", String.valueOf(board.rows()));
            boardElement.setAttribute("cols", String.valueOf(board.cols()));
            for (Vehicle vehicle : board.vehicles()) {
                Element element = document.createElement(VEHICLE);
                element.setAttribute("id", String.valueOf(vehicle.id()));
                element.setAttribute("row", String.valueOf(vehicle.start().row()));
                element.setAttribute("col", String.valueOf(vehicle.start().col()));
                element.setAttribute("length", String.valueOf(vehicle.length()));
                element.setAttribute("orientation", vehicle.orientation().name());
                boardElement.appendChild(element);
            }
            root.appendChild(boardElement);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            try (OutputStream out = Files.newOutputStream(path)) {
                transformer.transform(new DOMSource(document), new StreamResult(out));
            }
        } catch (ParserConfigurationException | TransformerException e) {
            throw new IOException("Nem sikerült XML-t készíteni.", e);
        }
    }

    /**
     * @param path a fájl útvonala
     * @return a betöltött játék
     * @throws IOException ha a fájl nem olvasható
     * @throws BoardFormatException ha a fájl tartalma hibás
     */
    public SavedGame load(Path path) throws IOException, BoardFormatException {
        Document document;
        try (InputStream in = Files.newInputStream(path)) {
            document = newDocumentBuilder().parse(in);
        } catch (SAXException | ParserConfigurationException e) {
            throw new BoardFormatException("Hibás XML fájl: " + e.getMessage());
        }
        try {
            Element root = document.getDocumentElement();
            if (!GAME.equals(root.getTagName())) {
                throw new BoardFormatException("A gyökérelem nem <game>.");
            }
            String playerName = singleChild(root, PLAYER).getTextContent().strip();
            Element boardElement = singleChild(root, BOARD);
            List<Vehicle> vehicles = new ArrayList<>();
            NodeList vehicleNodes = boardElement.getElementsByTagName(VEHICLE);
            for (int i = 0; i < vehicleNodes.getLength(); i++) {
                vehicles.add(toVehicle((Element) vehicleNodes.item(i)));
            }
            Board board = new Board(intAttribute(boardElement, "rows"), intAttribute(boardElement, "cols"), vehicles);
            return new SavedGame(playerName, board);
        } catch (IllegalArgumentException e) {
            throw new BoardFormatException("Hibás mentés: " + e.getMessage());
        }
    }

    private Vehicle toVehicle(Element element) {
        String id = element.getAttribute("id");
        if (id.length() != 1) {
            throw new IllegalArgumentException("érvénytelen járműazonosító: '" + id + "'");
        }
        Position start = new Position(intAttribute(element, "row"), intAttribute(element, "col"));
        Orientation orientation = Orientation.valueOf(element.getAttribute("orientation"));
        return new Vehicle(id.charAt(0), start, intAttribute(element, "length"), orientation);
    }

    private Element singleChild(Element parent, String tagName) throws BoardFormatException {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() != 1) {
            throw new BoardFormatException("Pontosan egy <" + tagName + "> elem kell.");
        }
        return (Element) nodes.item(0);
    }

    private int intAttribute(Element element, String name) {
        return Integer.parseInt(element.getAttribute(name));
    }

    /**
     * Olyan XML-olvasót készít, amely nem tölt be külső entitásokat és DOCTYPE-ot (XXE támadás ellen).
     */
    private DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setExpandEntityReferences(false);
        return factory.newDocumentBuilder();
    }
}
