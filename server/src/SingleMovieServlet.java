import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8"); // Response mime type
        response.setCharacterEncoding("UTF-8");

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // Output stream to STDOUT

        PrintWriter out = response.getWriter();

        PreparedStatement getMovie = null;
        PreparedStatement getGenre = null;
        PreparedStatement getStar = null;
        PreparedStatement getCount = null;

        String movieQuery = "SELECT * FROM (movies LEFT OUTER JOIN ratings r on movies.id = r.movieId) WHERE movies.id = ?;";
        String genreQuery = "SELECT name FROM genres_in_movies, genres WHERE movieId = ? AND id = genreID;";
        String starQuery = "SELECT name, starId FROM stars_in_movies, stars WHERE movieId = ? AND id = starID";
        String countQuery = "SELECT COUNT(*) FROM stars_in_movies WHERE Starid = ?;";

        try {

            Connection con = dataSource.getConnection();

            getMovie = con.prepareStatement(movieQuery);
            getMovie.setString(1,id);
            ResultSet rs = getMovie.executeQuery();

            if (!rs.next()) {
                JsonObject object = new JsonObject();
                object.addProperty("errorMessage", "movie with id '" + id + "' not found");
                out.write(object.toString());
                response.setStatus(404);
                getMovie.close();
                con.close();
                out.close();
                return;
            }

            JsonArray movieGenres = new JsonArray();


            getGenre = con.prepareStatement(genreQuery);
            getGenre.setString(1,id);
            ResultSet genre_rs = getGenre.executeQuery();

            // Iterate through each row of rs
            while (genre_rs.next()) movieGenres.add(genre_rs.getString("name"));

            genre_rs.close();
            getGenre.close();


            getStar = con.prepareStatement(starQuery);
            getStar.setString(1,id);
            ResultSet star_rs = getStar.executeQuery();

            getCount = con.prepareStatement(countQuery);

            ArrayList<Star> list = new ArrayList<>();
            while (star_rs.next()) {
                getCount.setString(1,star_rs.getString("starId"));
                ResultSet count_rs = getCount.executeQuery();
                while (count_rs.next()) list.add(new Star(star_rs.getString("name"), star_rs.getString("starId"), Integer.parseInt(count_rs.getString("COUNT(*)"))));
                count_rs.close();
            }

            getCount.close();

            Collections.sort(list, Comparator.comparing(Star::getCount).thenComparing(Star::getName));

            JsonArray movie_star = new JsonArray();

            // Iterate through each row of rs
            for (int i = 0; i < list.size(); i++) {
                String star_id = list.get(i).getId();
                String star_name = list.get(i).getName();
                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("star_id", star_id);
                jsonObject.addProperty("star_name", star_name);
                movie_star.add(jsonObject);
            }


            // Create a JsonObject based on the data we retrieve from rs
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("movie_id", rs.getString("id"));
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



            star_rs.close();
            getStar.close();
            rs.close();
            getMovie.close();
            con.close();
        } catch (Exception e) {
            // write error message JSON object to output
            out.write(Util.exception2Json(e).toString());

            // set reponse status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        out.close();
    }
}