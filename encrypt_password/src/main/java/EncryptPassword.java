import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

import java.sql.*;
import java.util.ArrayList;

public class EncryptPassword {
    public static void main(String[] args) throws SQLException {
        final String USER = args[0];
        final String PASSWORD = args[1];
        final String TABLE_NAME = args[2];
        final String PRIMARY_KEY_COL = args[3];

        final String LOGIN_URL = "jdbc:mysql://localhost:3306/moviedb";
        Connection connection = DriverManager.getConnection(LOGIN_URL, USER, PASSWORD);
        Statement statement = connection.createStatement();

        // change the customers table password column from VARCHAR(20) to VARCHAR(128)
        String alterQuery = "ALTER TABLE " + TABLE_NAME + " MODIFY COLUMN password VARCHAR(128)";
        int alterResult = statement.executeUpdate(alterQuery);
        System.out.println("altering customers table schema completed, " + alterResult + " rows affected");

        // get the ID and password for each customer
        String query = "SELECT " + PRIMARY_KEY_COL + ", password FROM " + TABLE_NAME;
        ResultSet rs = statement.executeQuery(query);

        // we use the StrongPasswordEncryptor from jasypt library (Java Simplified Encryption)
        // it internally use SHA-256 algorithm and 10,000 iterations to calculate the encrypted password
        PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
        ArrayList<String> updateQueryList = new ArrayList<>();

        System.out.println("encrypting password (this might take a while)");
        while (rs.next()) {
            // get the ID and plain text password from current table
            String id = rs.getString(PRIMARY_KEY_COL);
            String password = rs.getString("password");

            // encrypt the password using StrongPasswordEncryptor
            String encryptedPassword = passwordEncryptor.encryptPassword(password);

            // generate the update query
            String updateQuery = "UPDATE " + TABLE_NAME + " SET password = '" + encryptedPassword + "' WHERE " + PRIMARY_KEY_COL + " = '" + id + "'";
            updateQueryList.add(updateQuery);
        }
        rs.close();

        // execute the update queries to update the password
        System.out.println("updating password");
        int count = 0;
        for (String updateQuery : updateQueryList) {
            int updateResult = statement.executeUpdate(updateQuery);
            count += updateResult;
        }
        System.out.println("updating password completed, " + count + " rows affected");

        statement.close();
        connection.close();

        System.out.println("finished");
    }
}
