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

// Declaring a WebServlet called MoviesServlet, which maps to url "/api/movies"
@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {

    // Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8"); // Response mime type
        response.setCharacterEncoding("UTF-8");
        String title    = request.getParameter("title");
        String year     = request.getParameter("year");
        String director = request.getParameter("director");
        String star     = request.getParameter("star");
        String genre    = request.getParameter("genre");
        String alnum    = request.getParameter("alnum");
        String sort     = request.getParameter("sort");
        String page     = request.getParameter("page");
        String display  = request.getParameter("display");

        HttpSession session = request.getSession();

        PreparedStatement getMovie = null;
        PreparedStatement getGenre = null;
        PreparedStatement getStar = null;
        PreparedStatement getStarcount = null;


        String movieQuery = "SELECT * FROM movies, ";
        String genreQuery = "SELECT name FROM genres_in_movies, genres WHERE movieId = ? AND id = genreID ORDER BY name LIMIT 3;";
        String starQuery = "SELECT name, starId FROM stars_in_movies, stars WHERE movieId = ? AND id = starID;";
        String starcountQuery = "SELECT COUNT(*) FROM stars_in_movies WHERE Starid = ?;";


        boolean t  = !title.equals("")    && !title.equals(null)    && !title.equals("null"),
                y  = year.length() == 4   && !year.equals(null)     && !year.equals("null"),
                s  = !star.equals("")     && !star.equals(null)     && !star.equals("null"),
                d  = !director.equals("") && !director.equals(null) && !director.equals("null"),
                a  = !alnum.equals("")    && !alnum.equals(null)    && !alnum.equals("null"),
                g  = !genre.equals("")    && !genre.equals(null)    && !genre.equals("null"),
                p  = !page.equals("")     && !page.equals(null)     && !page.equals("null"),
                di = !display.equals("")  && !display.equals(null)  && !display.equals("null"),
                st = !sort.equals("")     && !sort.equals(null)     && !sort.equals("null");


        if (!di) {
            String sdisplay = (String) session.getAttribute("display");
            if (sdisplay == null) sdisplay = "25";
            display = sdisplay;
        }


        if (st) {
            session.setAttribute("sort", sort);
        } else {
            sort = (String) session.getAttribute("sort");
            if (sort == null) {
                session.setAttribute("sort", "1");
                sort = "1";
            }
        }
        String orderby = "";
        if (sort.equals("1")) orderby = "rating ASC, title ASC";
        else if (sort.equals("2")) orderby = "rating ASC, title DESC";
        else if (sort.equals("3")) orderby = "rating DESC, title ASC";
        else if (sort.equals("4")) orderby = "rating DESC, title DESC";
        else if (sort.equals("5")) orderby = "title ASC, rating ASC";
        else if (sort.equals("6")) orderby = "title ASC, rating DESC";
        else if (sort.equals("7")) orderby = "title DESC, rating ASC";
        else if (sort.equals("8")) orderby = "title DESC, rating DESC";

        session.setAttribute("display", display);

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

        try {
            Connection con = dataSource.getConnection();

            getGenre = con.prepareStatement(genreQuery);
            getStar = con.prepareStatement(starQuery);
            getStarcount = con.prepareStatement(starcountQuery);

            String sortQuery = " Limit 25;";

            if (alnum.equals("*")) {
                movieQuery += "ratings WHERE movies.id = ratings.movieId AND movies.title REGEXP '^[^a-z0-9]'";
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
            }
            else if (a) {
                movieQuery += "ratings WHERE movies.id = ratings.movieId AND movies.title LIKE ?";
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, alnum);

            }
            else if (g) {
                movieQuery += "genres_in_movies, genres , ratings WHERE movies.id = ratings.movieId AND movies.id= genres_in_movies.movieid AND genres_in_movies.genreId= genres.id AND name LIKE ?";
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, genre);
            }
            else if ( t && !y && !s && !d) {
                movieQuery += "ratings WHERE movies.id = ratings.movieId AND movies.title LIKE '%?%'";
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, title);
            }
            else if (!t &&  y && !s && !d) {
                movieQuery += "ratings WHERE movies.id = ratings.movieId AND movies.year = ?";
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, year);
            }
            else if (!t && !y && !s &&  d) {
                movieQuery += "ratings WHERE movies.id = ratings.movieId AND movies.director LIKE '%?%'";
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, director);
            }
            else if (!t && !y &&  s && !d) {
                movieQuery += "stars_in_movies, stars , ratings WHERE movies.id = ratings.movieId AND movies.id= stars_in_movies.movieid AND stars_in_movies.starId= stars.id AND name LIKE '%?%'";
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, star);
            }
            else if ( t &&  y && !s && !d) {
                movieQuery += "ratings WHERE movies.id = ratings.movieId AND movies.year = ? AND movies.title LIKE '%?%'";
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, year);
                getMovie.setString(2, title);
            }
            else if ( t && !y && !s &&  d) {
                movieQuery += "ratings WHERE movies.id = ratings.movieId AND movies.director LIKE '%?%' AND movies.title LIKE '%?%'";
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, director);
                getMovie.setString(1, title);
            }
            else if ( t && !y &&  s && !d) {
                movieQuery += "stars_in_movies, stars , ratings WHERE movies.id = ratings.movieId AND movies.id= stars_in_movies.movieid AND stars_in_movies.starId= stars.id AND movies.title LIKE '%?%' AND name LIKE '%?%'";
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, title);
                getMovie.setString(2, star);
            }
            else if (!t &&  y && !s &&  d) {
                movieQuery += "ratings WHERE movies.id = ratings.movieId AND movies.year = ? AND movies.director LIKE '%?%'";
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, year);
                getMovie.setString(1, director);
            }
            else if (!t &&  y &&  s && !d) {
                movieQuery += "stars_in_movies, stars , ratings WHERE movies.id = ratings.movieId AND movies.id= stars_in_movies.movieid AND stars_in_movies.starId= stars.id AND movies.year = ? AND name LIKE '%?%'";
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, year);
                getMovie.setString(1, star);
            }
            else if (!t && !y &&  s &&  d) {
                movieQuery += "stars_in_movies, stars , ratings WHERE movies.id = ratings.movieId AND movies.id= stars_in_movies.movieid AND stars_in_movies.starId= stars.id AND movies.director LIKE '%?%' AND name LIKE '%?%'";
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, director);
                getMovie.setString(1, star);
            }
            else if ( t && !y &&  s &&  d){
                movieQuery += "stars_in_movies, stars , ratings WHERE movies.id = ratings.movieId AND movies.id= stars_in_movies.movieid AND stars_in_movies.starId= stars.id AND movies.title LIKE '%"+title+"%' AND movies.director LIKE '%"+director+"%' AND name LIKE '%"+star+"%'";
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, director);
                getMovie.setString(1, star);
            }
            else if (!t &&  y &&  s &&  d){
                movieQuery += "stars_in_movies, stars , ratings WHERE movies.id = ratings.movieId AND movies.id= stars_in_movies.movieid AND stars_in_movies.starId= stars.id AND movies.year = '"+year+"%' AND movies.director LIKE '%"+director+"%' AND name LIKE '%"+star+"%'";
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, director);
                getMovie.setString(1, star);
            }
            else if ( t &&  y &&  s && !d){
                movieQuery += "stars_in_movies, stars , ratings WHERE movies.id = ratings.movieId AND movies.id= stars_in_movies.movieid AND stars_in_movies.starId= stars.id AND movies.year = '"+year+"%' AND movies.title LIKE '%"+title+"%' AND name LIKE '%"+star+"%'";
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, director);
                getMovie.setString(1, star);
            }
            else if ( t &&  y && !s &&  d){
                movieQuery += "ratings WHERE movies.id = ratings.movieId AND movies.year = '"+year+"' AND movies.director LIKE '%"+director+"%' AND movies.title LIKE '%"+title+"%'";
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, director);
                getMovie.setString(1, star);
            }
            else if ( t &&  y &&  s &&  d){
                movieQuery += "stars_in_movies, stars , ratings WHERE movies.id = ratings.movieId AND movies.id= stars_in_movies.movieid AND stars_in_movies.starId= stars.id AND movies.director LIKE '%"+director+"%' AND movies.year = '"+year+"%' AND movies.title LIKE '%"+title+"%' AND name LIKE '%"+star+"%'";
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, director);
                getMovie.setString(1, star);
            }

            String squery = (String) session.getAttribute("query");



            if (!p) {
                String spage = (String) session.getAttribute("page");
                if (spage == null) spage = "1";
                page = spage;
            }

            if (page.equals("0")) page = "1";


            session.setAttribute("page", page);
            int offset = (Integer.parseInt(page) - 1) * Integer.parseInt(display);


            //query += " ORDER BY " + orderby + "  LIMIT "+ display + " OFFSET "+ offset;



            JsonObject ret = new JsonObject();


            if (true) //!query.equals(squery)
            {
                //session.setAttribute("query", query);

                // Perform the query
                ResultSet movie_rs = getMovie.executeQuery();

                JsonArray moviesArray = new JsonArray();
                // Iterate through each row of rs
                while (movie_rs.next()) {
                    String movieId = movie_rs.getString("id");
                    JsonArray movieGenres = new JsonArray();


                    getGenre.setString(1,movieId);
                    ResultSet genre_rs = getGenre.executeQuery();

                    while (genre_rs.next()) movieGenres.add(genre_rs.getString("name"));
                    genre_rs.close();

                    getStar.setString(1,movieId);
                    ResultSet star_rs = getStar.executeQuery();

                    ArrayList<Star> list = new ArrayList<>();
                    while (star_rs.next()) {
                        getStarcount.setString(1,star_rs.getString("starId"));
                        ResultSet count_rs = getStarcount.executeQuery();
                        while (count_rs.next()) list.add(new Star(star_rs.getString("name"), star_rs.getString("starId"), Integer.parseInt(count_rs.getString("COUNT(*)"))));
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
                    while (star_rs.next()) {
                        String star_id = star_rs.getString("starId");
                        String star_name = star_rs.getString("name");
                        // Create a JsonObject based on the data we retrieve from rs
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("starId", star_id);
                        jsonObject.addProperty("starName", star_name);
                        movieStar.add(jsonObject);
                    }

                    // Create a JsonObject based on the data we retrieve from rs
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("movieId", movieId);
                    jsonObject.addProperty("movieTitle", movie_rs.getString("title"));
                    jsonObject.addProperty("movieYear", movie_rs.getString("year"));
                    jsonObject.addProperty("movieDirector", movie_rs.getString("director"));
                    jsonObject.add("movieGenres", movieGenres);
                    jsonObject.add("movieStars", movieStar);
                    jsonObject.addProperty("movieRating", movie_rs.getString("rating"));
                    moviesArray.add(jsonObject);

                    star_rs.close();
                }

                getGenre.close();
                getStar.close();

                movie_rs.close();
                getMovie.close();

                JsonObject jpage = new JsonObject();
                jpage.addProperty("page", page);
                ret.add("movies", moviesArray);
                ret.add("customer", Customer.toJSON((Customer) request.getSession().getAttribute("customer")));
                ret.add("page", jpage);
                session.setAttribute("ret", ret);
            }
            else
            {
                ret = (JsonObject) session.getAttribute("ret");
                System.out.println("cached");
            }
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