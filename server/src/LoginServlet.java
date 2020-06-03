import com.google.gson.JsonObject;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
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


@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse response) throws IOException {
        req.getSession().invalidate();
        response.setContentType("application/json; charset=UTF-8"); // Response mime type
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();


        String header = req.getHeader("User-Agent");
        if (!header.startsWith("Dalvik")) {
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
        }
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String type = req.getParameter("type");
        if (type == null || (!type.equals("customer") && !type.equals("employee"))) {
            JsonObject jsonObject = Util.makeGeneralErrorJsonObject("incorrect type specified");
            out.write(jsonObject.toString());
            out.close();
            response.setStatus(400);
            return;
        }
        try {
            // Context initContext = new InitialContext();
            // Context envContext = (Context) initContext.lookup("java:/comp/env");
            // DataSource ds = (DataSource) envContext.lookup("jdbc/moviedb");
            Connection connection = dataSource.getConnection();

            PreparedStatement emailStatement = type.equals("customer") ?
                    connection.prepareStatement("SELECT * FROM customers WHERE email = ?") :
                    connection.prepareStatement("SELECT * FROM employees WHERE email = ?");
            emailStatement.setString(1, email);
            ResultSet resultSet = emailStatement.executeQuery();
            JsonObject ret = new JsonObject();

            if (!resultSet.next() || !new StrongPasswordEncryptor().checkPassword(password, resultSet.getString("password"))) {
                ret.addProperty("status", "fail");
                ret.addProperty("message", "invalid email address or password");
                JsonObject jsonObject = Util.makeGeneralErrorJsonObject("invalid email address or password");
                out.write(jsonObject.toString());
                out.close();
                response.setStatus(400);
                return;
            } else {
                if (type.equals("customer")) {
                    Customer customer = new Customer(
                            resultSet.getInt("id"),
                            resultSet.getString("firstName")
                    );
                    req.getSession().setAttribute("customer", customer);
                    ret.addProperty("status", "success");
                    JsonObject customerJson = new JsonObject();
                    customerJson.addProperty("id", customer.id);
                    customerJson.addProperty("firstName", customer.firstName);
                    ret.add("customer", customerJson);
                } else {
                    Employee employee = new Employee(
                            resultSet.getString("email"),
                            resultSet.getString("fullname")
                    );
                    req.getSession().setAttribute("employee", employee);
                    ret.addProperty("status", "success");
                    JsonObject employeeJson = new JsonObject();
                    employeeJson.addProperty("id", employee.email);
                    employeeJson.addProperty("firstName", employee.fullName);
                    ret.add("employee", employeeJson);
                }
                response.setStatus(200);
            }
            out.write(ret.toString());
        } catch (Exception e) {
            // write error message JSON object to output
            out.write(Util.exception2Json(e).toString());

            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        out.close();
    }
}
