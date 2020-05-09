import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException, IOException, SAXException, ParserConfigurationException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/moviedbexample",
                "mytestuser",
                "mypassword"
        );

        StarParser.parse(connection);
    }
}
