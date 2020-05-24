package fabflixmobile;

public class Movie {
    private String id;
    private String title;
    private short year;
    private String director;

    public Movie(String id, String title, short year, String director) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
    }


    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public short getYear() {
        return year;
    }

    public String getDirector() {
        return director;
    }
}