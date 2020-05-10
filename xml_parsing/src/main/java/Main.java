import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args)
            throws SQLException, IOException, SAXException, ParserConfigurationException, TransformerException {
        Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/moviedbexample",
                "mytestuser",
                "mypassword"
        );

        StarParser.parse(connection);
        MovieParser.parse(connection);
    }
}
