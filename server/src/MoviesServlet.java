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

    // Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8"); // Response mime type
        response.setCharacterEncoding("UTF-8");
        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String star = request.getParameter("star");
        String genre = request.getParameter("genre");
        String alnum = request.getParameter("alnum");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try (Connection connection = dataSource.getConnection();
             Statement movieStatement = connection.createStatement();
             Statement genreStatement = connection.createStatement();
             Statement starStatement = connection.createStatement()
        ) {
            String query="SELECT * FROM movies, ";
            boolean t = (!title.equals("")), y = (year.length() == 4), s = (!star.equals("")), d = (!director.equals(""));
            if (alnum.equals("*")) query += "ratings WHERE movies.id = ratings.movieId AND movies.title REGEXP '^[^a-z0-9]'";
            else if (!alnum.equals("null")) query += "ratings WHERE movies.id = ratings.movieId AND movies.title LIKE '"+alnum+"%'";
            else if (!genre.equals("")) query += "genres_in_movies, genres , ratings WHERE movies.id = ratings.movieId AND movies.id= genres_in_movies.movieid AND genres_in_movies.genreId= genres.id AND name LIKE '"+genre+"'";
            else if (!t && !y && !s && !d) query += "ratings WHERE movies.id = ratings.movieId";
            else if ( t && !y && !s && !d) query += "ratings WHERE movies.id = ratings.movieId AND movies.title LIKE '%"+title+"%'";
            else if (!t &&  y && !s && !d) query += "ratings WHERE movies.id = ratings.movieId AND movies.year = '"+year+"'";
            else if (!t && !y && !s &&  d) query += "ratings WHERE movies.id = ratings.movieId AND movies.director LIKE '%"+director+"%'";
            else if (!t && !y &&  s && !d) query += "stars_in_movies, stars , ratings WHERE movies.id = ratings.movieId AND movies.id= stars_in_movies.movieid AND stars_in_movies.starId= stars.id AND name LIKE '%"+star+"%'";
            else if ( t &&  y && !s && !d) query += "ratings WHERE movies.id = ratings.movieId AND movies.year = '"+year+"' AND movies.title LIKE '%"+title+"%'";
            else if ( t && !y && !s &&  d) query += "ratings WHERE movies.id = ratings.movieId AND movies.director LIKE '%"+director+"%' AND movies.title LIKE '%"+title+"%'";
            else if ( t && !y &&  s && !d) query += "stars_in_movies, stars , ratings WHERE movies.id = ratings.movieId AND movies.id= stars_in_movies.movieid AND stars_in_movies.starId= stars.id AND movies.title LIKE '%"+title+"%' AND name LIKE '%"+star+"%'";
            else if (!t &&  y && !s &&  d) query += "ratings WHERE movies.id = ratings.movieId AND movies.year = '"+year+"' AND movies.director LIKE '%"+director+"%'";
            else if (!t &&  y &&  s && !d) query += "stars_in_movies, stars , ratings WHERE movies.id = ratings.movieId AND movies.id= stars_in_movies.movieid AND stars_in_movies.starId= stars.id AND movies.year = '"+year+"%' AND name LIKE '%"+star+"%'";
            else if (!t && !y &&  s &&  d) query += "stars_in_movies, stars , ratings WHERE movies.id = ratings.movieId AND movies.id= stars_in_movies.movieid AND stars_in_movies.starId= stars.id AND movies.director LIKE '%"+director+"%' AND name LIKE '%"+star+"%'";
            else if ( t && !y &&  s &&  d) query += "stars_in_movies, stars , ratings WHERE movies.id = ratings.movieId AND movies.id= stars_in_movies.movieid AND stars_in_movies.starId= stars.id AND movies.title LIKE '%"+title+"%' AND movies.director LIKE '%"+director+"%' AND name LIKE '%"+star+"%'";
            else if (!t &&  y &&  s &&  d) query += "stars_in_movies, stars , ratings WHERE movies.id = ratings.movieId AND movies.id= stars_in_movies.movieid AND stars_in_movies.starId= stars.id AND movies.year = '"+year+"%' AND movies.director LIKE '%"+director+"%' AND name LIKE '%"+star+"%'";
            else if ( t &&  y &&  s && !d) query += "stars_in_movies, stars , ratings WHERE movies.id = ratings.movieId AND movies.id= stars_in_movies.movieid AND stars_in_movies.starId= stars.id AND movies.year = '"+year+"%' AND movies.title LIKE '%"+title+"%' AND name LIKE '%"+star+"%'";
            else if ( t &&  y && !s &&  d) query += "ratings WHERE movies.id = ratings.movieId AND movies.year = '"+year+"' AND movies.director LIKE '%"+director+"%' AND movies.title LIKE '%"+title+"%'";
            else if ( t &&  y &&  s &&  d) query += "stars_in_movies, stars , ratings WHERE movies.id = ratings.movieId AND movies.id= stars_in_movies.movieid AND stars_in_movies.starId= stars.id AND movies.director LIKE '%"+director+"%' AND movies.year = '"+year+"%' AND movies.title LIKE '%"+title+"%' AND name LIKE '%"+star+"%'";
            query+= " ORDER BY title  LIMIT 100";
            System.out.println("query: " + query);
            // Perform the query
            ResultSet movieResultSet = movieStatement.executeQuery(query);

            JsonObject ret = new JsonObject();
            JsonArray moviesArray = new JsonArray();

            // Iterate through each row of rs
            while (movieResultSet.next()) {
                String movieId = movieResultSet.getString("id");
                JsonArray movieGenres = new JsonArray();

                query = "SELECT name FROM genres_in_movies, genres WHERE movieId = '" + movieId + "' AND id = genreID ORDER BY name LIMIT 3";

                // Perform the query
                ResultSet genreResultSet = genreStatement.executeQuery(query);

                // Iterate through each row of rs
                while (genreResultSet.next()) {
                    movieGenres.add(genreResultSet.getString("name"));
                }

                query = "SELECT name, starId FROM stars_in_movies, stars WHERE movieId = '" + movieId +
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

            ret.add("movies", moviesArray);
            ret.add("customer", Customer.toJSON((Customer) request.getSession().getAttribute("customer")));

            // write JSON string to output
            out.write(ret.toString());
            // set response status to 200 (OK)
            response.setStatus(200);
        } catch (Exception e) {

            // write error message JSON object to output
            out.write(Util.exception2Json(e).toString());

            // set reponse status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        out.close();
    }
}