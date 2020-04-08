import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;


@WebServlet(name = "MovieListServlet", urlPatterns = "/api/movies")
public class MovieListServlet extends HttpServlet {

    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM movies LIMIT 20");

            JsonArray movieList = new JsonArray();

            while (resultSet.next()) {
                JsonObject movieRecord = new JsonObject();
                movieRecord.addProperty("movieId", resultSet.getString("id"));
                movieRecord.addProperty("movieTitle", resultSet.getString("title"));
                movieRecord.addProperty("movieYear", resultSet.getString("year"));
                movieRecord.addProperty("movieDirector", resultSet.getString("director"));
                movieList.add(movieRecord);
            }

            out.write(movieList.toString());
            resp.setStatus(200);

            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            resp.setStatus(500);
        }
    }
}
