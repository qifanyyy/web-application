import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class StarsInMoviesParser {
    private static final String XML_URI = "../stanford-movies/casts124.xml";

    static void parse(@NotNull Connection connection)
            throws IOException, SAXException, ParserConfigurationException, TransformerException, SQLException {
        Element docElement = Util.getDocumentElementFromXmlUri(XML_URI);
        NodeList castList = docElement.getElementsByTagName("m");
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
                System.err.println("no matching movie for movieTitle='" + movieTitle + '\'');
                System.err.println(Util.nodeToXmlFormatString(filmcElement) + "\n");
                getMovieStatement.close();
                continue;
            }
            String movieId = movieResult.getString(1);
            getMovieStatement.close();

            PreparedStatement getStarStatement = connection.prepareStatement("SELECT id FROM stars WHERE name = ?");
            getStarStatement.setString(1, starName);
            ResultSet starResult = getStarStatement.executeQuery();
            if (!starResult.next()) {
                System.err.println("no matching star for starName='" + starName + '\'');
                System.err.println(Util.nodeToXmlFormatString(filmcElement) + "\n");
                getStarStatement.close();
                continue;
            }
            String starId = starResult.getString(1);
            getStarStatement.close();

            PreparedStatement insertStarInMovieStatement = connection.prepareStatement(
                    "INSERT INTO stars_in_movies VALUES (?, ?)"
            );
            insertStarInMovieStatement.setString(1, starId);
            insertStarInMovieStatement.setString(2, movieId);
            try {
                insertStarInMovieStatement.executeUpdate();
            } catch (SQLException e) {
                // ref: https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-error-sqlstates.html
                if (e.getSQLState().equals("23000")) {
                    System.err.println("adding duplicate entry (" + starId + "," + movieId + ") into stars_in_movies\n");
                } else {
                    throw e;
                }
            }
        }
    }
}
