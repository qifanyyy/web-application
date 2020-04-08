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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

// Declaring a WebServlet called MoviesServlet, which maps to url "/api/movies"
@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            // Declare our statement
            Statement statement = dbcon.createStatement();

            String query = "SELECT * FROM movies, ratings WHERE movies.id = ratings.movieId ORDER BY -rating LIMIT 20;";

            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("id");
                String movie_genre = "";

                query = "SELECT name FROM genres_in_movies, genres WHERE movieId = '"+ movie_id + "' AND id = genreID;";

                // Declare our statement
                Statement genre_statement = dbcon.createStatement();

                // Perform the query
                ResultSet genre_rs = genre_statement.executeQuery(query);

                // Iterate through each row of rs
                while (genre_rs.next()) {
                    if (movie_genre != "") movie_genre += ", ";
                    movie_genre += genre_rs.getString("name");
                }
                genre_rs.close();
                genre_statement.close();

                query = "SELECT name, starId FROM stars_in_movies, stars WHERE movieId = '"+ movie_id + "' AND id = starID LIMIT 3;";

                JsonArray movie_star = new JsonArray();

                // Declare our statement
                Statement star_statement = dbcon.createStatement();

                // Perform the query
                ResultSet star_rs = star_statement.executeQuery(query);

                // Iterate through each row of rs
                while (star_rs.next()) {
                    String star_id = star_rs.getString("starId");
                    String star_name = star_rs.getString("name");
                    // Create a JsonObject based on the data we retrieve from rs
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("star_id", star_id);
                    jsonObject.addProperty("star_name", star_name);
                    movie_star.add(jsonObject);
                }
                star_rs.close();
                star_statement.close();

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", rs.getString("title"));
                jsonObject.addProperty("movie_year", rs.getString("year"));
                jsonObject.addProperty("movie_director", rs.getString("director"));
                jsonObject.addProperty("movie_genre", movie_genre);
                jsonObject.addProperty("movie_star", movie_star.toString());
                jsonObject.addProperty("movie_rating", rs.getString("rating"));
                jsonArray.add(jsonObject);
            }
            
            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

            rs.close();
            statement.close();
            dbcon.close();
        } catch (Exception e) {
        	
			// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());

			// set reponse status to 500 (Internal Server Error)
			response.setStatus(500);
        }
        out.close();
    }
}