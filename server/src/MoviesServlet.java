import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

// Declaring a WebServlet called MoviesServlet, which maps to url "/api/movies"
@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long timerTSStart = System.nanoTime();

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
        String fulltext = request.getParameter("fulltext");
        String fuzzy    = request.getParameter("fuzzy");
        String manualPage = request.getParameter("manualPage");

        System.out.println(fuzzy);

        HttpSession session = request.getSession();

        PreparedStatement getMovie = null;
        PreparedStatement getGenre;
        PreparedStatement getStar;
        PreparedStatement getStarcount;

        String movieQuery = "SELECT * FROM movies, ";
        String genreQuery = "SELECT name FROM genres_in_movies, genres WHERE movieId = ? AND id = genreID ORDER BY name LIMIT 3;";
        String starQuery = "SELECT name, starId FROM stars_in_movies, stars WHERE movieId = ? AND id = starID;";
        String starcountQuery = "SELECT COUNT(*) FROM stars_in_movies WHERE Starid = ?;";


        boolean t  = title != null    && !title.equals("")    && !title.equals("null"),
                y  = year != null     && year.length() == 4   && !year.equals("null"),
                s  = star != null     && !star.equals("")     && !star.equals("null"),
                d  = director != null && !director.equals("") && !director.equals("null"),
                a  = alnum != null    && !alnum.equals("")    && !alnum.equals("null"),
                g  = genre != null    && !genre.equals("")    && !genre.equals("null"),
                p  = page != null     && !page.equals("")     && !page.equals("null"),
                di = display != null  && !display.equals("")  && !display.equals("null"),
                st = sort != null     && !sort.equals("")     && !sort.equals("null");


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


        if (!p) {
            String spage = (String) session.getAttribute("page");
            if (spage == null) spage = "1";
            page = spage;
        }
        if (manualPage == null && (t || page.equals("0"))) page = "1";

        session.setAttribute("page", page);

        if (!t && !y && !s && !d && !a && !g){
            title    = (String) session.getAttribute("title");
            year     = (String) session.getAttribute("year");
            director = (String) session.getAttribute("director");
            star     = (String) session.getAttribute("star");
            genre    = (String) session.getAttribute("genre");
            alnum    = (String) session.getAttribute("alnum");

            t = title != null    && !title.equals("")    && !title.equals("null");
            y = year != null     && year.length() == 4   && !year.equals("null");
            s = star != null     && !star.equals("")     && !star.equals("null");
            d = director != null && !director.equals("") && !director.equals("null");
            a = alnum != null    && !alnum.equals("")    && !alnum.equals("null");
            g = genre != null    && !genre.equals("")    &&!genre.equals("null");
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
        long timerTJStart = System.nanoTime();

        try {
            // the following few lines are for connection pooling
            // Obtain our environment naming context
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/r");
            Connection con = ds.getConnection();

            getGenre = con.prepareStatement(genreQuery);
            getStar = con.prepareStatement(starQuery);
            getStarcount = con.prepareStatement(starcountQuery);



            int offset = (Integer.parseInt(page) - 1) * Integer.parseInt(display);

            String sortQuery = " ORDER BY ? LIMIT ? OFFSET ?;";
            String titleQuery = " AND movies.title LIKE ?";
            String fuzzyQuery = " OR min_edit_distance('" + title + "', title) <= 2)";





            String processedtitle = "%" + title + "%";
            if (fulltext.equals("fulltextsearch")) {
                processedtitle = "";
                String[] fulltextarray = title.split(" ");
                for (int i = 0; i < fulltextarray.length; i++) processedtitle += "+" + fulltextarray[i] + "* ";
                titleQuery = " AND match(title) against (? IN BOOLEAN MODE)";
            }

            if (fuzzy.equals("Fuzzyon")) {
                titleQuery = titleQuery.substring(0, 5) + '(' + titleQuery.substring(5);
                titleQuery += fuzzyQuery;
            }

            if (alnum.equals("*")) {
                movieQuery += "ratings WHERE movies.id = ratings.movieId AND movies.title REGEXP '^[^a-z0-9]'";
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, orderby);
                getMovie.setInt(2, Integer.parseInt(display));
                getMovie.setInt(3, offset);
            }
            else if (a) {
                movieQuery += "ratings WHERE movies.id = ratings.movieId AND movies.title LIKE ?";
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, alnum + "%");
                getMovie.setString(2, orderby);
                getMovie.setInt(3, Integer.parseInt(display));
                getMovie.setInt(4, offset);

            }
            else if (g) {
                movieQuery += "genres_in_movies, genres , ratings WHERE movies.id = ratings.movieId AND movies.id= genres_in_movies.movieid AND genres_in_movies.genreId= genres.id AND name LIKE ?";
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, genre);
                getMovie.setString(2, orderby);
                getMovie.setInt(3, Integer.parseInt(display));
                getMovie.setInt(4, offset);
            }
            else if ( t && !y && !s && !d) {
                movieQuery += "ratings WHERE movies.id = ratings.movieId" + titleQuery;
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, processedtitle);
                getMovie.setString(2, orderby);
                getMovie.setInt(3, Integer.parseInt(display));
                getMovie.setInt(4, offset);
            }
            else if (!t &&  y && !s && !d) {
                movieQuery += "ratings WHERE movies.id = ratings.movieId AND movies.year = ?";
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, year);
                getMovie.setString(2, orderby);
                getMovie.setInt(3, Integer.parseInt(display));
                getMovie.setInt(4, offset);
            }
            else if (!t && !y && !s &&  d) {
                movieQuery += "ratings WHERE movies.id = ratings.movieId AND movies.director LIKE ?";
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, "%" + director + "%");
                getMovie.setString(2, orderby);
                getMovie.setInt(3, Integer.parseInt(display));
                getMovie.setInt(4, offset);
            }
            else if (!t && !y &&  s && !d) {
                movieQuery += "stars_in_movies, stars , ratings WHERE movies.id = ratings.movieId AND movies.id= stars_in_movies.movieid AND stars_in_movies.starId= stars.id AND name LIKE ?";
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, "%"+ star + "%");
                getMovie.setString(2, orderby);
                getMovie.setInt(3, Integer.parseInt(display));
                getMovie.setInt(4, offset);
            }
            else if ( t &&  y && !s && !d) {
                movieQuery += "ratings WHERE movies.id = ratings.movieId AND movies.year = ?" + titleQuery;
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, year);
                getMovie.setString(2, "%"+ title + "%");
                getMovie.setString(3, orderby);
                getMovie.setInt(4, Integer.parseInt(display));
                getMovie.setInt(5, offset);
            }
            else if ( t && !y && !s &&  d) {
                movieQuery += "ratings WHERE movies.id = ratings.movieId AND movies.director LIKE ?" + titleQuery;
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, "%" + director + "%");
                getMovie.setString(2, processedtitle);
                getMovie.setString(3, orderby);
                getMovie.setInt(4, Integer.parseInt(display));
                getMovie.setInt(5, offset);
            }
            else if ( t && !y &&  s && !d) {
                movieQuery += "stars_in_movies, stars , ratings WHERE movies.id = ratings.movieId AND movies.id= stars_in_movies.movieid AND stars_in_movies.starId= stars.id AND name LIKE ?" + titleQuery;
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);

                getMovie.setString(1, "%" + star + "%");
                getMovie.setString(2, processedtitle);
                getMovie.setString(3, orderby);
                getMovie.setInt(4, Integer.parseInt(display));
                getMovie.setInt(5, offset);
            }
            else if (!t &&  y && !s &&  d) {
                movieQuery += "ratings WHERE movies.id = ratings.movieId AND movies.year = ? AND movies.director LIKE ?";
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, year);
                getMovie.setString(2, "%" + director + "%");
                getMovie.setString(3, orderby);
                getMovie.setInt(4, Integer.parseInt(display));
                getMovie.setInt(5, offset);
            }
            else if (!t &&  y &&  s && !d) {
                movieQuery += "stars_in_movies, stars , ratings WHERE movies.id = ratings.movieId AND movies.id= stars_in_movies.movieid AND stars_in_movies.starId= stars.id AND movies.year = ? AND name LIKE ?";
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, year);
                getMovie.setString(2, "%" + star + "%");
                getMovie.setString(3, orderby);
                getMovie.setInt(4, Integer.parseInt(display));
                getMovie.setInt(5, offset);
            }
            else if (!t && !y &&  s &&  d) {
                movieQuery += "stars_in_movies, stars , ratings WHERE movies.id = ratings.movieId AND movies.id= stars_in_movies.movieid AND stars_in_movies.starId= stars.id AND movies.director LIKE ? AND name LIKE ?";
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, "%" + director + "%");
                getMovie.setString(2, "%" + star + "%");
                getMovie.setString(3, orderby);
                getMovie.setInt(4, Integer.parseInt(display));
                getMovie.setInt(5, offset);
            }
            else if ( t && !y &&  s &&  d){
                movieQuery += "stars_in_movies, stars , ratings WHERE movies.id = ratings.movieId AND movies.id= stars_in_movies.movieid AND stars_in_movies.starId= stars.id AND movies.director LIKE ? AND name LIKE ?" + titleQuery;
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);

                getMovie.setString(1, "%" + director + "%");
                getMovie.setString(2, "%" + star + "%");
                getMovie.setString(3, processedtitle);
                getMovie.setString(4, orderby);
                getMovie.setInt(5, Integer.parseInt(display));
                getMovie.setInt(6, offset);
            }
            else if (!t &&  y &&  s &&  d){
                movieQuery += "stars_in_movies, stars , ratings WHERE movies.id = ratings.movieId AND movies.id= stars_in_movies.movieid AND stars_in_movies.starId= stars.id AND movies.year = ? AND movies.director LIKE ? AND name LIKE ?";
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, year);
                getMovie.setString(2, "%" + director + "%");
                getMovie.setString(3, "%" + star + "%");
                getMovie.setString(4, orderby);
                getMovie.setInt(5, Integer.parseInt(display));
                getMovie.setInt(6, offset);
            }
            else if ( t &&  y &&  s && !d){
                movieQuery += "stars_in_movies, stars , ratings WHERE movies.id = ratings.movieId AND movies.id= stars_in_movies.movieid AND stars_in_movies.starId= stars.id AND movies.year = ? AND name LIKE ?" + titleQuery;
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, year);
                getMovie.setString(2, "%" + star + "%");
                getMovie.setString(3, processedtitle);
                getMovie.setString(4, orderby);
                getMovie.setInt(5, Integer.parseInt(display));
                getMovie.setInt(6, offset);
            }
            else if ( t &&  y && !s &&  d){
                movieQuery += "ratings WHERE movies.id = ratings.movieId AND movies.year = ? AND movies.director LIKE ?" + titleQuery;
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, year);
                getMovie.setString(2, "%" + director + "%");
                getMovie.setString(3, processedtitle);
                getMovie.setString(4, orderby);
                getMovie.setInt(5, Integer.parseInt(display));
                getMovie.setInt(6, offset);
            }
            else if ( t &&  y &&  s &&  d){
                movieQuery += "stars_in_movies, stars , ratings WHERE movies.id = ratings.movieId AND movies.id= stars_in_movies.movieid AND stars_in_movies.starId= stars.id AND movies.director LIKE ? AND movies.year = ? AND name LIKE ?" + titleQuery;
                movieQuery += sortQuery;
                getMovie = con.prepareStatement(movieQuery);
                getMovie.setString(1, "%" + director + "%");
                getMovie.setString(2, year);
                getMovie.setString(3, "%" + star + "%");
                getMovie.setString(4, processedtitle);
                getMovie.setString(5, orderby);
                getMovie.setInt(6, Integer.parseInt(display));
                getMovie.setInt(7, offset);
            }

            JsonObject data = new JsonObject();
            data.addProperty("title", title);
            data.addProperty("year", year);
            data.addProperty("director", director);
            data.addProperty("star", star);
            data.addProperty("genre", genre);
            data.addProperty("alnum", alnum);
            data.addProperty("sort", sort);
            data.addProperty("page", page);
            data.addProperty("display", display);
            data.addProperty("fuzzy", fuzzy);
            JsonObject squery = (JsonObject) session.getAttribute("data");

            JsonObject ret = new JsonObject();
            if (!data.equals(squery))
            {
                session.setAttribute("data", data);
                System.out.println(getMovie);
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
                movie_rs.close();


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

            getGenre.close();
            getStar.close();
            if (getMovie != null) getMovie.close();
            con.close();
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

        JsonObject logObj = new JsonObject();
        long timerEnd = System.nanoTime();
        logObj.addProperty("TS", timerEnd - timerTSStart);
        logObj.addProperty("TJ", timerEnd - timerTJStart);
        logObj.addProperty("timestamp", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()));
        String urlQueryString = request.getQueryString();
        logObj.addProperty("url", request.getRequestURL().toString() + (urlQueryString == null ? "" : "?" + urlQueryString));
        try {
            final Path path = Paths.get(getServletContext().getRealPath("/") + "moviesServletLog.txt");
            Files.write(
                    path,
                    Collections.singletonList(logObj.toString()),
                    StandardCharsets.UTF_8,
                    Files.exists(path) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE
            );
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}