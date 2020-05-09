import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

class StarParser {
    private static final String XmlUri = "../stanford-movies/actors63.xml";
    private static final Set<Star> stars = new HashSet<>();

    private StarParser() {}

    static void parse(Connection connection)
            throws ParserConfigurationException, IOException, SAXException, SQLException {
        Element docElement = Util.getDocumentElementFromXmlUri(XmlUri);
        NodeList nodeList = docElement.getElementsByTagName("actor");
        int i;
        for (i = 0; i < nodeList.getLength(); ++i) {
            Element element;
            if ((element = (Element) nodeList.item(i)) == null) {
                System.err.println("nodeList contains a node which can't be cast to Element");
                continue;
            }

            String name = Util.getTextValueFromTagInElement(element, "stagename");
            Integer birthYear = null;
            try {
                birthYear = Util.getIntValueFromTagInElement(element, "dob");
            } catch (NumberFormatException ignored) {}

            Star star = new Star(name, birthYear);
            if (stars.contains(star)) {
                System.err.println("duplicate star '" + star + "' found; skipping");
                continue;
            }

            stars.add(star);
        }
        if (i == 0) {
            System.err.println("docElement.getElementsByTagName(\"actor\") returned zero-length NodeList");
            return;
        }

        i = 0;
        for (Star star : stars) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO stars VALUES (?, ?, ?)");
            statement.setString(1, "xs_" + i++);
            statement.setString(2, star.name);
            statement.setObject(3, star.birthYear);
            statement.executeUpdate();
            statement.close();
        }
    }
}
