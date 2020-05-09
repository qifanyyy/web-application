import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8"); // Response mime type
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String gRecaptchaResponse = req.getParameter("g-recaptcha-response");
        try {
            Util.verifyRecaptcha(gRecaptchaResponse);
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("status", "fail");
            jsonObject.addProperty("message", "reCaptcha authentication failed");
            jsonObject.add("exception", Util.exception2Json(e));
            out.write(jsonObject.toString());
            out.close();
            response.setStatus(400);
            return;
        }

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement emailStatement = connection.prepareStatement("SELECT * FROM customers WHERE email = ?")
        ) {
            emailStatement.setString(1, email);
            ResultSet userResultSet = emailStatement.executeQuery();
            JsonObject jsonObject = new JsonObject();

            if (!userResultSet.next() || !new StrongPasswordEncryptor().checkPassword(password, userResultSet.getString("password"))) {
                jsonObject.addProperty("status", "fail");
                jsonObject.addProperty("message", "invalid email address or password");
                response.setStatus(400);
            } else {
                Customer customer = new Customer(
                        userResultSet.getInt("id"),
                        userResultSet.getString("firstName")
                );
                req.getSession().setAttribute("customer", customer);
                jsonObject.addProperty("status", "success");
                JsonObject customerJSON = new JsonObject();
                customerJSON.addProperty("id", customer.id);
                customerJSON.addProperty("firstName", customer.firstName);
                jsonObject.add("customer", customerJSON);
                response.setStatus(200);
            }
            out.write(jsonObject.toString());
        } catch (Exception e) {
            // write error message JSON object to output
            out.write(Util.exception2Json(e).toString());

            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        out.close();
    }
}
