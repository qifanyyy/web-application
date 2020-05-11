import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.sql.*;

public class Main {
    public static void main(String[] args)
            throws SQLException, IOException, SAXException, ParserConfigurationException, TransformerException, InterruptedException {

        Thread parseStarThread = new Thread(() -> {
            try {
                StarParser.parse();
            } catch (ParserConfigurationException | IOException | SAXException | SQLException | TransformerException e) {
                e.printStackTrace();
                System.exit(1);
            }
        });

        Thread parseMovieThread = new Thread(() -> {
            try {
                MovieParser.parse();
            } catch (ParserConfigurationException | IOException | SAXException | SQLException | TransformerException | InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
        parseStarThread.start();
        parseMovieThread.start();
        parseStarThread.join();
        parseMovieThread.join();
        StarsInMoviesParser.parse();
    }
}
