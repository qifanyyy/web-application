import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.sql.*;
import java.util.*;

class MovieParser {
    private static final String XML_URI = "../stanford-movies/mains243.xml";
    private static final Map<String, String> CAT_CODE_MAPPING;
    private static final LinkedHashSet<Movie> MOVIES = new LinkedHashSet<>();
    private static final List<Set<String>> GENRES_IN_MOVIES = new ArrayList<>();

    static {
        CAT_CODE_MAPPING = new HashMap<>();
        CAT_CODE_MAPPING.put("actn", "Violence");
        CAT_CODE_MAPPING.put("advt", "Adventure");
        CAT_CODE_MAPPING.put("avga", "Avant Grade");
        CAT_CODE_MAPPING.put("camp", "Now - Camp");
        CAT_CODE_MAPPING.put("cart", "Cartoon");
        CAT_CODE_MAPPING.put("cnr", "Cops and Robbers");
        CAT_CODE_MAPPING.put("comd", "Comedy");
        CAT_CODE_MAPPING.put("disa", "Disaster");
        CAT_CODE_MAPPING.put("docu", "Documentary");
        CAT_CODE_MAPPING.put("dram", "Drama");
        CAT_CODE_MAPPING.put("epic", "Epic");
        CAT_CODE_MAPPING.put("faml", "Family");
        CAT_CODE_MAPPING.put("hist", "History");
        CAT_CODE_MAPPING.put("horr", "Horror");
        CAT_CODE_MAPPING.put("musc", "Musical");
        CAT_CODE_MAPPING.put("myst", "Mystery");
        CAT_CODE_MAPPING.put("noir", "Black");
        CAT_CODE_MAPPING.put("porn", "Pornography");
        CAT_CODE_MAPPING.put("romt", "Romantic");
        CAT_CODE_MAPPING.put("scfi", "Science Fiction");
        CAT_CODE_MAPPING.put("surl", "Sureal");
        CAT_CODE_MAPPING.put("susp", "Thriller");
        CAT_CODE_MAPPING.put("west", "Western");

        CAT_CODE_MAPPING.put("s.f.", "Science Fiction");
        CAT_CODE_MAPPING.put("biop", "Biographical Picture");
        CAT_CODE_MAPPING.put("tv", "TV Show");
        CAT_CODE_MAPPING.put("tvs", "TV Series");
        CAT_CODE_MAPPING.put("tvm", "TV Miniseries");
        CAT_CODE_MAPPING.put("fant", "Fantasy");
        CAT_CODE_MAPPING.put("cnrb", "Cops and Robbers");
    }

    private MovieParser() {}

    @NotNull
    private static Set<String> getGenresFromFilm(@NotNull Element film) {
        Set<String> ret = new LinkedHashSet<>();

        NodeList catsList = film.getElementsByTagName("cats");
        for (int i = 0; i < catsList.getLength(); ++i) {
            NodeList catList = ((Element) catsList.item(i)).getElementsByTagName("cat");
            for (int j = 0; j < catList.getLength(); ++j) {
                Element catElement  = (Element) catList.item(j);
                Node catTextNode = catElement.getFirstChild();
                if (catTextNode == null) {  // self-closed cat tag from catElement
                    continue;
                }
                String catCode = catTextNode.getNodeValue();
                if (catCode == null) {  // abnormal cat tag content from catElement
                    continue;
                }
                catCode = catCode.trim().toLowerCase(Locale.ENGLISH);

                if (!CAT_CODE_MAPPING.containsKey(catCode)) {  // unknown catCode from catElement
                    continue;
                }

                String category = CAT_CODE_MAPPING.get(catCode);

                ret.add(category);
            }
        }

        return ret;
    }

    @Nullable
    private static String getDirectorFromDirectorFilm(@NotNull Element directorFilm) {
        NodeList directorList = directorFilm.getElementsByTagName("director");
        if (directorList.getLength() == 0) {
            return null;
        }

        Element director = (Element) directorList.item(0);

        return Util.getTextValueFromTagInElement(director, "dirname");
    }

    static void parse()
            throws IOException, SAXException, ParserConfigurationException, TransformerException, SQLException, InterruptedException {
        Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/moviedb",
                "mytestuser",
                "mypassword"
        );
        Element docElement = Util.getDocumentElementFromXmlUri(XML_URI);
        NodeList directorFilmsList = docElement.getElementsByTagName("directorfilms");
        int i;
        for (i = 0; i < directorFilmsList.getLength(); ++i) {
            Element directorFilmElement = (Element) directorFilmsList.item(i);
            NodeList filmList = directorFilmElement.getElementsByTagName("film");
            int j;

            String director = getDirectorFromDirectorFilm(directorFilmElement);

            for (j = 0; j < filmList.getLength(); ++j) {
                Element filmElement = (Element) filmList.item(j);

                String title = Util.getTextValueFromTagInElement(filmElement, "t");
                Integer year = null;
                try {
                    year = Util.getIntValueFromTagInElement(filmElement, "year");
                } catch (NumberFormatException ignored) {}
                Set<String> genres = getGenresFromFilm(filmElement);

                if (title == null || (title = title.trim()).length() == 0) {
                    System.err.println("null or empty title for film at i=" + i + ",j=" + j + " when parsing");
                    System.err.println("\t" + Util.nodeToXmlFormatString(filmElement) + "\n");
                    continue;
                }

                if (year == null) {
                    System.err.println("invalid year for film at i=" + i + ",j=" + j + " when parsing");
                    System.err.println("\t" + Util.nodeToXmlFormatString(filmElement) + "\n");
                    continue;
                }

                if (director == null || (director = director.trim()).length() == 0) {
                    System.err.println("null or empty director for film at i=" + i + ",j=" + j + " when parsing");
                    System.err.println("\t" + Util.nodeToXmlFormatString(filmElement) + "\n");
                    continue;
                }

                Movie movie = new Movie(title, year, director);
                if (MOVIES.contains(movie)) {
                    // System.err.println("duplicate movie " + movie + " found; skipping\n");
                    continue;
                }

                MOVIES.add(movie);
                GENRES_IN_MOVIES.add(genres);
            }

            if (j == 0) {
                System.err.println(
                        "directorFilmElement.getElementsByTagName(\"film\") returned zero-length NodeList when i=" + i
                );
                System.err.println("\t" + Util.nodeToXmlFormatString(directorFilmElement) + "\n");
            }
        }

        if (i == 0) {
            System.err.println("docElement.getElementsByTagName(\"directorfilms\") returned zero-length NodeList");
            return;
        }

        assert MOVIES.size() == GENRES_IN_MOVIES.size();
        Iterator<Movie> movieIterator = MOVIES.iterator();
        List<String> movieIdList = new ArrayList<>(MOVIES.size());

        PreparedStatement insertMovie = connection.prepareStatement("INSERT INTO movies VALUES (?, ?, ?, ?)");
        Connection connection2 = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/moviedb",
                "mytestuser",
                "mypassword"
        );
        PreparedStatement insertRating = connection2.prepareStatement("INSERT INTO ratings VALUES (?, 0.0, 0)");
        for (i = 0; i < MOVIES.size(); ++i) {
            Movie movie = movieIterator.next();

            PreparedStatement checkDup = connection.prepareStatement("SELECT * FROM movies WHERE title = ? AND year = ? AND director = ?");
            checkDup.setString(1, movie.title);
            checkDup.setInt(2, movie.year);
            checkDup.setString(3, movie.director);
            if (checkDup.executeQuery().next()) {
                // System.err.println("movie " + movie + " exists in db\n");
                checkDup.close();
                continue;
            }
            checkDup.close();

            String movieId = "xm_" + i;

            insertMovie.setString(1, movieId);
            insertMovie.setString(2, movie.title);
            insertMovie.setInt(3, movie.year);
            insertMovie.setString(4, movie.director);
            insertMovie.addBatch();

            insertRating.setString(1, movieId);
            insertRating.addBatch();

            movieIdList.add(movieId);
        }
        insertMovie.executeBatch();
        insertMovie.close();
        Thread t = new Thread(() -> {
            try {
                insertRating.executeBatch();
                insertRating.close();
            } catch (SQLException e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
        t.start();

        Iterator<String> movieIdIterator = movieIdList.iterator();
        Iterator<Set<String>> genresInMoviesIterator = GENRES_IN_MOVIES.iterator();
        for (i = 0; i < GENRES_IN_MOVIES.size(); ++i) {
            Set<String> genres = genresInMoviesIterator.next();
            String movieId = movieIdIterator.next();

            PreparedStatement insertGenre = connection.prepareStatement(
                    "INSERT IGNORE INTO genres VALUES (NULL, ?)"
            );
            for (String genre : genres) {
                insertGenre.setString(1, genre);
                insertGenre.addBatch();
            }
            insertGenre.executeBatch();
            insertGenre.close();

            PreparedStatement insertGenreInMovie = connection.prepareStatement(
                    "INSERT INTO genres_in_movies VALUES (?, ?)"
            );
            for (String genre : genres) {
                PreparedStatement getLastInsertId = connection.prepareStatement("SELECT id FROM genres WHERE name = ?");
                getLastInsertId.setString(1, genre);
                ResultSet idRS = getLastInsertId.executeQuery();
                idRS.next();
                int genreId = idRS.getInt(1);
                getLastInsertId.close();


                insertGenreInMovie.setInt(1, genreId);
                insertGenreInMovie.setString(2, movieId);
                insertGenreInMovie.addBatch();
            }
            insertGenreInMovie.executeBatch();
            insertGenreInMovie.close();
        }
        t.join();
        connection2.close();
        connection.close();
    }
}
