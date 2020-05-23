package edu.uci.ics.fabflixmobile;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SingleMovie extends ActionBarActivity {
    private final static String url = "https://10.0.2.2:8443/server_war/api/single-movie?id=";
    private Movie movie = null;

    private static class Movie {
        final String title;
        final int year;
        final String director;
        final double rating;
        final List<String> genres;
        final List<Star> stars;

        Movie(String title, int year, String director, double rating, List<String> genres, List<Star> stars) {
            this.title = title;
            this.year = year;
            this.director = director;
            this.rating = rating;
            this.genres = genres;
            this.stars = stars;
        }

        static Movie fromJson(JSONObject jsonObject) throws JSONException {
            JSONArray starArray = jsonObject.getJSONArray("movie_star");
            List<Star> stars = new ArrayList<>(starArray.length());
            for (int i = 0; i < starArray.length(); ++i) {
                stars.add(Star.fromJSON((JSONObject) starArray.get(i)));
            }

            JSONArray genreArray = jsonObject.getJSONArray("movie_genre");
            List<String> genres = new ArrayList<>(genreArray.length());
            for (int i = 0; i < genreArray.length(); ++i) {
                genres.add(genreArray.getString(i));
            }

            return new Movie(
                    jsonObject.getString("movie_title"),
                    Integer.parseInt(jsonObject.getString("movie_year")),
                    jsonObject.getString("movie_director"),
                    Double.parseDouble(jsonObject.getString("movie_rating")),
                    genres,
                    stars
            );
        }

        String getGenreDisplayString() {
            return String.join(", ", genres);
        }

        String getStarDisplayString() {
            return stars.stream().map(star -> star.name).collect(Collectors.joining("\n"));
        }
    }

    private static class Star {
        final String name;

        Star(String name) {
            this.name = name;
        }

        static Star fromJSON(JSONObject jsonObject) throws JSONException {
            return new Star(jsonObject.getString("star_name"));
        }
    }

    private void getMovieInfo(String movieId) {
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        final StringRequest singleMovieReq = new StringRequest(Request.Method.GET, url + movieId, response -> {
            try {
                JSONObject responseJson = new JSONObject(response);
                movie = Movie.fromJson(responseJson);
                displayMovieInfo();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, err -> Log.e("SingleMovie.error", err.toString()));
        queue.add(singleMovieReq);
    }

    private void displayMovieInfo() {
        TextView movieTitleTextView = findViewById(R.id.movie_title);
        TextView movieRatingTextView = findViewById(R.id.movie_rating);
        TextView movieDirectorTextView = findViewById(R.id.movie_director);
        TextView movieGenresTextView = findViewById(R.id.genres_list_content);
        TextView movieStarsTextView = findViewById(R.id.stars_list_content);

        runOnUiThread(() -> {
            movieTitleTextView.setText(movie.title + " (" + movie.year + ')');
            movieRatingTextView.setText(new DecimalFormat("#.0").format(movie.rating));
            movieDirectorTextView.setText(movie.director);
            movieGenresTextView.setText(movie.getGenreDisplayString());
            movieStarsTextView.setText(movie.getStarDisplayString());
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_movie);

        getMovieInfo("tt0126029");
    }
}
