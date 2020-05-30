import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
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

@WebServlet(name = "GenresServlet", urlPatterns = "/api/genres")
public class GenresServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8"); // Response mime type
        resp.setCharacterEncoding("UTF-8");

        // Output stream to STDOUT
        PrintWriter out = resp.getWriter();

        try {
            // the following few lines are for connection pooling
            // Obtain our environment naming context
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/r");

            Connection connection = ds.getConnection();
            PreparedStatement genreStatement = connection.prepareStatement("SELECT * FROM genres");
            ResultSet resultSet = genreStatement.executeQuery();
            JsonArray genresJSON = new JsonArray();

            while (resultSet.next()) {
                JsonObject genre = new JsonObject();
                genre.addProperty("id", resultSet.getInt("id"));
                genre.addProperty("name", resultSet.getString("name"));
                genresJSON.add(genre);
            }
            genreStatement.close();
            resultSet.close();
            connection.close();
            out.write(genresJSON.toString());
            resp.setStatus(200);
        } catch (Exception e) {
            out.write(Util.exception2Json(e).toString());
            resp.setStatus(500);
        }
        out.close();
    }
}
