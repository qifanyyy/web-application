import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

// Declaring a WebServlet called MoviesServlet, which maps to url "/api/movies"
@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {

    // Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8"); // Response mime type
        response.setCharacterEncoding("UTF-8");
        String title       = request.getParameter("title");
        String year        = request.getParameter("year");
        String director    = request.getParameter("director");
        String star        = request.getParameter("star");
        String genre       = request.getParameter("genre");
        String alnum       = request.getParameter("alnum");
        String sort        = request.getParameter("sort");
        String page        = request.getParameter("page");
        String display     = request.getParameter("display");

        HttpSession session = request.getSession();
        String sessionId = session.getId();

        String sort2 = (String) session.getAttribute("sort");
        String order = (String) session.getAttribute("order");
        String sort_sec = ", rating";

        boolean t  = !title.equals("")    && !title.equals(null)    && !title.equals("null"),
                y  = year.length() == 4   && !year.equals(null)     && !year.equals("null"),
                s  = !star.equals("")     && !star.equals(null)     && !star.equals("null"),
                d  = !director.equals("") && !director.equals(null) && !director.equals("null"),
                a  = !alnum.equals("")    && !alnum.equals(null)    && !alnum.equals("null"),
                g  = !genre.equals("")    && !genre.equals(null)    && !genre.equals("null"),
                p  = !page.equals("")     && !page.equals(null)     && !page.equals("null"),
                di = !display.equals("")  && !display.equals(null)  && !display.equals("null"),
                st = !sort.equals("")     && !sort.equals(null)     && !sort.equals("null"),
                ss = sort2 != null;

        if (!p) {
            String spage = (String) session.getAttribute("page");
            if (spage == null) spage = "1";
            page = spage;
        }

        if (!di) {
            String sdisplay = (String) session.getAttribute("display");
            if (sdisplay == null) sdisplay = "25";
            display = sdisplay;
        }

        if (order == null) order = " ASC";

        if (st){
            if (sort.equals(sort2)) {
                if (order.equals(" ASC")) order = " DESC";
                else order = " ASC";
            }
        } else if (ss) sort = sort2;
        else sort = "title";

        session.setAttribute("order", order);
        session.setAttribute("sort", sort);
        session.setAttribute("page", page);
        session.setAttribute("display", display);


        System.out.println(page);
        System.out.println(display);


        if (sort.equals("rating")) sort_sec = ", title";

        String orderby = sort + order + sort_sec;


        if (!t && !y && !s && !d && !a && !g){
            title    = (String) session.getAttribute("title");
            year     = (String) session.getAttribute("year");
            director = (String) session.getAttribute("director");
            star     = (String) session.getAttribute("star");
            genre    = (String) session.getAttribute("genre");
            alnum    = (String) session.getAttribute("alnum");

            t = !title.equals("")    && !title.equals(null)    && !title.equals("null");
            y = year.length() == 4   && !year.equals(null)     && !year.equals("null");
            s = !star.equals("")     && !star.equals(null)     && !star.equals("null");
            d = !director.equals("") && !director.equals(null) && !director.equals("null");
            a = !alnum.equals("")    && !alnum.equals(null)    && !alnum.equals("null");
            g = !genre.equals("")    && !genre.equals(null)    &&!genre.equals("null");
        } else {
            session.setAttribute("title", title);
            session.setAttribute("year", year);
            session.setAttribute("director", director);
            session.setAttribute("star", star);
            session.setAttribute("genre", genre);
            session.setAttribute("alnum", alnum);
        }


        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try (Connection connection = dataSource.getConnection();
             Statement movieStatement = connection.createStatement();
             Statement genreStatement = connection.createStatement();
             Statement starStatement = connection.createStatement();
             Statement countStatement = connection.createStatement();
        ) {
            String query="SELECT * FROM movies, ";

            if (alnum.equals("*")) query += "ratings WHERE movies.id = ratings.movieId AND movies.title REGEXP '^[^a-z0-9]'";
            else if (a) query += "ratings WHERE movies.id = ratings.movieId AND movies.title LIKE '"+alnum+"%'";
            else if (g) query += "genres_in_movies, genres , ratings WHERE movies.id = ratings.movieId AND movies.id= genres_in_movies.movieid AND genres_in_movies.genreId= genres.id AND name LIKE '"+genre+"'";
            else if (!t && !y && !s && !d) query += "ratings WHERE movies.id = ratings.movieId"; // delete
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
            query+= " ORDER BY " + orderby + "  LIMIT "+ display + " OFFSET "+Integer.toString((Integer.parseInt(page) - 1)*Integer.parseInt(display));
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

                query = "SELECT name, starId FROM stars_in_movies, stars WHERE movieId = '"+movieId+"' AND id = starID";
                ResultSet starResultSet = starStatement.executeQuery(query);
                ArrayList<Star> list = new ArrayList<>();
                while (starResultSet.next()) {
                    query = "SELECT COUNT(*) FROM stars_in_movies WHERE Starid = '"+ starResultSet.getString("starId") +"'";
                    ResultSet countResultSet = countStatement.executeQuery(query);
                    while (countResultSet.next()) {
                        list.add(new Star(starResultSet.getString("name"), starResultSet.getString("starId"), Integer.parseInt(countResultSet.getString("COUNT(*)"))));
                    }
                }
                Collections.sort(list, Comparator.comparing(Star::getCount).thenComparing(Star::getName));

                JsonArray movieStar = new JsonArray();
                for (int i = 0; i < Math.min(list.size(), 3); i++) {
                    String star_id = list.get(i).getId();
                    String star_name = list.get(i).getName();
                    // Create a JsonObject based on the data we retrieve from rs
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("starId", star_id);
                    jsonObject.addProperty("starName", star_name);
                    movieStar.add(jsonObject);
                }
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