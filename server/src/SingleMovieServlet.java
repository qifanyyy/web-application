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
import java.sql.ResultSet;
import java.sql.Statement;

// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-movie"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json; charset=UTF-8"); // Response mime type
        response.setCharacterEncoding("UTF-8");

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            // Declare our statement
            Statement statement = dbcon.createStatement();
            // Construct a query with parameter represented by "?"
            String query = "SELECT * FROM (movies LEFT OUTER JOIN ratings r on movies.id = r.movieId) " + "WHERE movies.id ='" + id + "'";
            System.out.println("query: " + query);
            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            if (!rs.next()) {
                JsonObject object = new JsonObject();
                object.addProperty("errorMessage", "movie with id '" + id + "' not found");
                out.write(object.toString());
                response.setStatus(404);
                statement.close();
                dbcon.close();
                out.close();
                return;
            }

            JsonArray movieGenres = new JsonArray();
            query = "SELECT name FROM genres_in_movies, genres WHERE movieId = '" + id + "' AND id = genreID";

            // Declare our statement
            Statement genre_statement = dbcon.createStatement();
            // Perform the query
            ResultSet genre_rs = genre_statement.executeQuery(query);

            // Iterate through each row of rs
            while (genre_rs.next()) {
                movieGenres.add(genre_rs.getString("name"));
            }
            genre_rs.close();
            genre_statement.close();

            query = "SELECT name, starId FROM stars_in_movies, stars WHERE movieId = '" + id + "' AND id = starID;";
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
            jsonObject.addProperty("movie_title", rs.getString("title"));
            jsonObject.addProperty("movie_year", rs.getString("year"));
            jsonObject.addProperty("movie_director", rs.getString("director"));
            jsonObject.add("movie_genre", movieGenres);
            jsonObject.add("movie_star", movie_star);
            jsonObject.addProperty("movie_rating", rs.getString("rating"));

            // write JSON string to output
            out.write(jsonObject.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

            rs.close();
            statement.close();
            dbcon.close();
        } catch (Exception e) {
            // write error message JSON object to output
            out.write(Util.exception2Json(e).toString());

            // set reponse status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        out.close();
    }
}