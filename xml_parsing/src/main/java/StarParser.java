import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

class StarParser {
    private static final String XML_URI = "../stanford-movies/actors63.xml";
    private static final Set<Star> STARS = new HashSet<>();

    private StarParser() {}

    static void parse(@NotNull Connection connection)
            throws ParserConfigurationException, IOException, SAXException, SQLException, TransformerException {
        Element docElement = Util.getDocumentElementFromXmlUri(XML_URI);
        NodeList nodeList = docElement.getElementsByTagName("actor");
        int i;
        for (i = 0; i < nodeList.getLength(); ++i) {
            Element element = (Element) nodeList.item(i);

            String name = Util.getTextValueFromTagInElement(element, "stagename");
            if (name == null) {
                System.err.println("cannot find stagename from current actor element");
                System.err.println("\t" + Util.nodeToXmlFormatString(element) + "\n");
                continue;
            } else if ((name = name.trim()).length() == 0) {
                System.err.println("empty stagename found from current actor element");
                System.err.println("\t" + Util.nodeToXmlFormatString(element) + "\n");
                continue;
            }

            Integer birthYear = null;
            try {
                birthYear = Util.getIntValueFromTagInElement(element, "dob");
            } catch (NumberFormatException e) {
                System.err.println("non-integer dob found; set null");
                System.err.println("\t" + Util.nodeToXmlFormatString(element) + "\n");
            }

            Star star = new Star(name, birthYear);
            if (STARS.contains(star)) {
                System.err.println("duplicate star '" + star + "' found; skipping\n");
                continue;
            }

            STARS.add(star);
        }
        if (i == 0) {
            System.err.println("docElement.getElementsByTagName(\"actor\") returned zero-length NodeList");
            return;
        }

        i = 0;
        for (Star star : STARS) {
            PreparedStatement checkDup = star.birthYear == null ?
                    connection.prepareStatement("SELECT * FROM stars WHERE name = ? AND birthYear IS NULL") :
                    connection.prepareStatement("SELECT * FROM stars WHERE name = ? AND birthYear = ?");
            checkDup.setString(1, star.name);
            if (star.birthYear != null) {
                checkDup.setObject(2, star.birthYear);
            }
            if (checkDup.executeQuery().next()) {
                System.err.println("star " + star + " exists in db");
                checkDup.close();
                continue;
            }
            checkDup.close();

            PreparedStatement statement = connection.prepareStatement("INSERT INTO stars VALUES (?, ?, ?)");
            statement.setString(1, "xs_" + i++);
            statement.setString(2, star.name);
            statement.setObject(3, star.birthYear);
            statement.executeUpdate();
            statement.close();
        }
    }
}
