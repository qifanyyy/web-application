import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.sql.*;

class StarsInMoviesParser {
    private static final String XML_URI = "../stanford-movies/casts124.xml";

    static void parse()
            throws IOException, SAXException, ParserConfigurationException, TransformerException, SQLException {
        Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/moviedb",
                "mytestuser",
                "mypassword"
        );
        Element docElement = Util.getDocumentElementFromXmlUri(XML_URI);
        NodeList castList = docElement.getElementsByTagName("m");
        PreparedStatement insertStarInMovieStatement = connection.prepareStatement(
                "INSERT IGNORE INTO stars_in_movies VALUES (?, ?)"
        );
        for (int i = 0; i < castList.getLength(); ++i) {
            Element filmcElement = (Element) castList.item(i);
            String movieTitle = Util.getTextValueFromTagInElement(filmcElement, "t");
            String starName = Util.getTextValueFromTagInElement(filmcElement, "a");

            if (movieTitle == null || (movieTitle = movieTitle.trim()).length() == 0) {
                System.err.println("null or empty movie title");
                System.err.println(Util.nodeToXmlFormatString(filmcElement) + "\n");
                continue;
            }

            if (starName == null || (starName = starName.trim()).length() == 0) {
                System.err.println("null or empty star name");
                System.err.println(Util.nodeToXmlFormatString(filmcElement) + "\n");
                continue;
            }

            PreparedStatement getMovieStatement = connection.prepareStatement("SELECT id FROM movies WHERE title = ?");
            getMovieStatement.setString(1, movieTitle);
            ResultSet movieResult = getMovieStatement.executeQuery();
            if (!movieResult.next()) {
                // System.err.println("no matching movie for movieTitle='" + movieTitle + '\'');
                // System.err.println(Util.nodeToXmlFormatString(filmcElement) + "\n");
                getMovieStatement.close();
                continue;
            }
            String movieId = movieResult.getString(1);
            getMovieStatement.close();

            PreparedStatement getStarStatement = connection.prepareStatement("SELECT id FROM stars WHERE name = ?");
            getStarStatement.setString(1, starName);
            ResultSet starResult = getStarStatement.executeQuery();
            if (!starResult.next()) {
                // System.err.println("no matching star for starName='" + starName + '\'');
                // System.err.println(Util.nodeToXmlFormatString(filmcElement) + "\n");
                getStarStatement.close();
                continue;
            }
            String starId = starResult.getString(1);
            getStarStatement.close();

            insertStarInMovieStatement.setString(1, starId);
            insertStarInMovieStatement.setString(2, movieId);
            insertStarInMovieStatement.addBatch();
        }
        insertStarInMovieStatement.executeBatch();
        insertStarInMovieStatement.close();
        connection.close();
    }
}
