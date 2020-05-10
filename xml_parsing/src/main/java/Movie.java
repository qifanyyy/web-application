import org.jetbrains.annotations.NotNull;

import java.util.Objects;

class Movie {
    @NotNull final String title;
    final int year;
    @NotNull final String director;

    public Movie(@NotNull String title, int year, @NotNull String director) {
        this.title = title;
        this.year = year;
        this.director = director;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "title='" + title + '\'' +
                ", year=" + year +
                ", director='" + director + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return year == movie.year &&
                title.equals(movie.title) &&
                director.equals(movie.director);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, year, director);
    }
}
