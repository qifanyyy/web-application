import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

// Declaring a WebServlet called MoviesServlet, which maps to url "/api/movies"
@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {

    // Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8"); // Response mime type
        response.setCharacterEncoding("UTF-8");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try (Connection connection = dataSource.getConnection();
             Statement movieStatement = connection.createStatement();
             Statement genreStatement = connection.createStatement();
             Statement starStatement = connection.createStatement()
        ) {
            String query = "SELECT * FROM movies, ratings WHERE movies.id = ratings.movieId ORDER BY -rating LIMIT 20";

            // Perform the query
            ResultSet movieResultSet = movieStatement.executeQuery(query);

            JsonArray moviesArray = new JsonArray();

            // Iterate through each row of rs
            while (movieResultSet.next()) {
                String movieId = movieResultSet.getString("id");
                JsonArray movieGenres = new JsonArray();

                query = "SELECT name FROM genres_in_movies, genres WHERE movieId = '"+ movieId + "' AND id = genreID";

                // Perform the query
                ResultSet genreResultSet = genreStatement.executeQuery(query);

                // Iterate through each row of rs
                while (genreResultSet.next()) {
                    movieGenres.add(genreResultSet.getString("name"));
                }

                query = "SELECT name, starId FROM stars_in_movies, stars WHERE movieId = '"+ movieId +
                        "' AND id = starID LIMIT 3";

                JsonArray movieStar = new JsonArray();

                // Perform the query
                ResultSet starResultSet = starStatement.executeQuery(query);

                // Iterate through each row of rs
                while (starResultSet.next()) {
                    String star_id = starResultSet.getString("starId");
                    String star_name = starResultSet.getString("name");
                    // Create a JsonObject based on the data we retrieve from rs
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("starId", star_id);
                    jsonObject.addProperty("starName", star_name);
                    movieStar.add(jsonObject);
                }

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movieId", movieId);
                jsonObject.addProperty("movieTitle", movieResultSet.getString("title"));
                jsonObject.addProperty("movieYear", movieResultSet.getString("year"));
                jsonObject.addProperty("movieDirector", movieResultSet.getString("director"));
                jsonObject.add("movieGenres", movieGenres);
                jsonObject.add("movieStars", movieStar);
                jsonObject.addProperty("movieRating", movieResultSet.getString("rating"));
                moviesArray.add(jsonObject);
            }
            
            // write JSON string to output
            out.write(moviesArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);
        } catch (Exception e) {
        	
			// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);
			jsonObject.addProperty("stackTrace", stringWriter.toString());
			out.write(jsonObject.toString());

			// set reponse status to 500 (Internal Server Error)
			response.setStatus(500);
        }
        out.close();
    }
}