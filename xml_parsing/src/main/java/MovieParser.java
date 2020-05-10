import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.sql.Connection;
import java.util.*;

class MovieParser {
    private static final String XML_URI = "../stanford-movies/mains243.xml";
    private static final Map<String, String> CAT_CODE_MAPPING;
    private static final Set<Movie> MOVIES = new HashSet<>();

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
    }

    private MovieParser() {}

    @NotNull
    private static List<String> getGenresFromFilm(@NotNull Element film) throws TransformerException {
        List<String> ret = new ArrayList<>();

        NodeList catsList = film.getElementsByTagName("cats");
        for (int i = 0; i < catsList.getLength(); ++i) {
            NodeList catList = ((Element) catsList.item(i)).getElementsByTagName("cat");
            for (int j = 0; j < catList.getLength(); ++j) {
                Element catElement  = (Element) catList.item(j);
                Node catTextNode = catElement.getFirstChild();
                if (catTextNode == null) {
                    // System.err.println("self-closed cat tag");
                    // System.err.println("\t" + Util.nodeToXmlFormatString(catElement) + "\n");
                    continue;
                }
                String catCode = catTextNode.getNodeValue();
                if (catCode == null) {
                    // System.err.println("weird cat tag");
                    // System.err.println("\t" + Util.nodeToXmlFormatString(catElement) + "\n");
                    continue;
                }
                catCode = catCode.trim().toLowerCase(Locale.ENGLISH);

                if (!CAT_CODE_MAPPING.containsKey(catCode)) {
                    // System.err.println(
                    //         "unknown catCode '" + catCode + "' at i=" + i + ",j=" + j + " inside getGenresFromFilm"
                    // );
                    // System.err.println("\t" + Util.nodeToXmlFormatString(catElement) + "\n");
                    continue;
                }

                String category = CAT_CODE_MAPPING.get(catCode);

                if (!ret.contains(category)) {
                    ret.add(category);
                }
            }
        }

        return ret;
    }

    @Nullable
    private static String getDirectorFromFilm(@NotNull Element film) throws TransformerException {
        NodeList dirsList = film.getElementsByTagName("dirs");
        for (int i = 0; i < dirsList.getLength(); ++i) {
            NodeList dirList = ((Element) dirsList.item(i)).getElementsByTagName("dir");
            for (int j = 0; j < dirList.getLength(); ++j) {
                String dirName = Util.getTextValueFromTagInElement((Element) dirList.item(j), "dirn");
                if (dirName != null && dirName.length() > 0) {
                    return dirName;
                }
                // System.err.println("null or empty dirName at i=" + i + ",j=" + j + " inside getDirectorFromFilm");
                // System.err.println("\t" + Util.nodeToXmlFormatString(dirList.item(j)) + "\n");
            }
        }

        return null;
    }

    static void parse(@NotNull Connection connection)
            throws IOException, SAXException, ParserConfigurationException, TransformerException {
        Element docElement = Util.getDocumentElementFromXmlUri(XML_URI);
        NodeList directorFilmsList = docElement.getElementsByTagName("directorfilms");
        int i;
        for (i = 0; i < directorFilmsList.getLength(); ++i) {
            Element directorFilmElement = (Element) directorFilmsList.item(i);

            NodeList filmList = directorFilmElement.getElementsByTagName("film");
            int j;
            for (j = 0; j < filmList.getLength(); ++j) {
                Element filmElement = (Element) filmList.item(j);

                String title = Util.getTextValueFromTagInElement(filmElement, "t");
                Integer year = null;
                try {
                    year = Util.getIntValueFromTagInElement(filmElement, "year");
                } catch (NumberFormatException ignored) {}
                List<String> genres = getGenresFromFilm(filmElement);
                String director = getDirectorFromFilm(filmElement);

                if (title == null || title.length() == 0) {
                    System.err.println("null or empty title for film at i=" + i + ",j=" + j + " when parsing");
                    System.err.println("\t" + Util.nodeToXmlFormatString(filmElement) + "\n");
                    continue;
                }

                if (year == null) {
                    System.err.println("invalid year for film at i=" + i + ",j=" + j + " when parsing");
                    System.err.println("\t" + Util.nodeToXmlFormatString(filmElement) + "\n");
                    continue;
                }

//                if (genres.size() == 0) {
//                    System.err.println("empty genre for film at i=" + i + ",j=" + j + " when parsing");
//                    System.err.println("\t" + Util.nodeToXmlFormatString(filmElement) + "\n");
//                    continue;
//                }

                if (director == null || director.length() == 0) {
                    System.err.println("null or empty director for film at i=" + i + ",j=" + j + " when parsing");
                    System.err.println("\t" + Util.nodeToXmlFormatString(filmElement) + "\n");
                    continue;
                }

                Movie movie = new Movie(title, year, director);
                if (MOVIES.contains(movie)) {
                    System.err.println("duplicate movie " + movie + " found; skipping");
                    continue;
                }

                MOVIES.add(movie);
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

//        for (Movie movie : MOVIES) {
//            System.out.println(movie);
//        }
        System.out.println(MOVIES.size());
    }
}
