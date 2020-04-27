import com.google.gson.JsonArray;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String firstName = req.getParameter("firstName");
        String lastName = req.getParameter("lastName");
        String id = req.getParameter("id");
        String expiration = req.getParameter("expiration");

        if (firstName == null || lastName == null || id == null || expiration == null) {
            resp.setStatus(400);
            PrintWriter out = resp.getWriter();
            out.write(Util.makeGeneralErrorJsonObject("missing payment parameters").toString());
            out.close();
            return;
        }

        LocalDate expirationDate;
        try {
            expirationDate = LocalDate.parse(expiration);
        } catch (DateTimeParseException e) {
            resp.setStatus(400);
            PrintWriter out = resp.getWriter();
            out.write(Util.makeGeneralErrorJsonObject("invalid date format").toString());
            out.close();
            return;
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement creditCardIdStatement = connection.prepareStatement("SELECT * FROM creditcards WHERE id = ?");
             PreparedStatement insertIntoSaleStatement = connection.prepareStatement(
                     // TODO: write to real sales table
                     "INSERT INTO sales_test (customerId, movieId, saleDate, quantity) VALUES (?, ?, ?, ?)",
                     PreparedStatement.RETURN_GENERATED_KEYS
             )) {
            creditCardIdStatement.setString(1, id);
            ResultSet resultSet = creditCardIdStatement.executeQuery();

            if (!resultSet.next() ||
                    !firstName.equals(resultSet.getString("firstName")) ||
                    !lastName.equals(resultSet.getString("lastName")) ||
                    // NOTE: I don't know why I have to add one day manually, but that's how jdbc works on my machine
                    !expirationDate.equals(resultSet.getDate("expiration").toLocalDate().plusDays(1))) {

                resp.setStatus(400);
                PrintWriter out = resp.getWriter();
                out.write(Util.makeGeneralErrorJsonObject("invalid credentials").toString());
                out.close();
                return;
            }

            Customer customer = (Customer) req.getSession().getAttribute("customer");
            @SuppressWarnings("unchecked")
            Map<String, CartItem> cart = (Map<String, CartItem>) req.getSession().getAttribute("cart");

            insertIntoSaleStatement.setInt(1, customer.id);
            insertIntoSaleStatement.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
            JsonObject ret = new JsonObject();
            ret.addProperty("status", "success");
            JsonArray sales = new JsonArray();

            for (String movieId : cart.keySet()) {
                JsonObject singleSale = new JsonObject();
                CartItem cartItem = cart.get(movieId);
                insertIntoSaleStatement.setString(2, movieId);
                insertIntoSaleStatement.setInt(4, cartItem.quantity);
                insertIntoSaleStatement.executeUpdate();
                ResultSet generatedIds = insertIntoSaleStatement.getGeneratedKeys();
                generatedIds.next();
                singleSale.addProperty("saleId", generatedIds.getInt("GENERATED_KEY"));
                singleSale.addProperty("movieId", movieId);
                singleSale.addProperty("movieTitle", cartItem.movieTitle);
                singleSale.addProperty("quantity", cartItem.quantity);
                sales.add(singleSale);
            }

            synchronized (req.getSession()) {
                cart.clear();
            }

            ret.add("sales", sales);
            resp.setStatus(200);
            PrintWriter out = resp.getWriter();
            out.write(ret.toString());
            out.close();
        } catch (Exception e) {
            resp.setStatus(500);
            PrintWriter out = resp.getWriter();
            out.write(Util.exception2Json(e).toString());
            out.close();
        }
    }
}
