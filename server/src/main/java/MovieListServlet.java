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

        try (Connection connection = dataSource.getConnection();
            Statement movieRecordQueryStatement = connection.createStatement();
            Statement genreQueryStatement = connection.createStatement();
            Statement starQueryStatement = connection.createStatement()
        ) {
            String movieRecordQuery = "SELECT * FROM (ratings JOIN movies m ON ratings.movieId = m.id) " +
                    "ORDER BY rating DESC LIMIT 20";
            ResultSet movieRecordResultSet = movieRecordQueryStatement.executeQuery(movieRecordQuery);

            JsonArray movieList = new JsonArray();

            while (movieRecordResultSet.next()) {
                JsonObject movieRecord = new JsonObject();
                String movieId = movieRecordResultSet.getString("id");
                movieRecord.addProperty("movieId", movieId);
                movieRecord.addProperty("movieTitle", movieRecordResultSet.getString("title"));
                movieRecord.addProperty("movieYear", movieRecordResultSet.getString("year"));
                movieRecord.addProperty("movieDirector", movieRecordResultSet.getString("director"));
                movieRecord.addProperty("movieRating", movieRecordResultSet.getString("rating"));

                String genreQuery = "SELECT * FROM (genres JOIN genres_in_movies gim ON genres.id = gim.genreId) " +
                        "WHERE movieId = '" + movieId + "' LIMIT 3";
                ResultSet genreResultSet = genreQueryStatement.executeQuery(genreQuery);
                JsonArray movieGenres = new JsonArray();
                while (genreResultSet.next()) {
                    movieGenres.add(genreResultSet.getString("name"));
                }
                movieRecord.add("movieGenres", movieGenres);

                String starQuery = "SELECT * FROM (stars JOIN stars_in_movies sim ON stars.id = sim.starId) " +
                        "WHERE movieId = '" + movieId + "' LIMIT 3";
                ResultSet starResultSet = starQueryStatement.executeQuery(starQuery);
                JsonArray movieStars = new JsonArray();
                while (starResultSet.next()) {
                    movieStars.add(starResultSet.getString("name"));
                }
                movieRecord.add("movieStars", movieStars);

                movieList.add(movieRecord);
            }

            out.write(movieList.toString());
            resp.setStatus(200);
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            resp.setStatus(500);
        }
    }
}
