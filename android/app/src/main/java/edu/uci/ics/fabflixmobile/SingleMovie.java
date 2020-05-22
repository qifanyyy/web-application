package edu.uci.ics.fabflixmobile;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SingleMovie extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_movie);

        final List<String> genres = new ArrayList<>();
        final List<String> stars = new ArrayList<>();
        genres.add("g1");
        genres.add("g2");

        stars.add("s1");
        stars.add("s2");

        TextView movieTitleTextView = findViewById(R.id.movie_title);
        TextView movieRatingTextView = findViewById(R.id.movie_rating);
        TextView movieDirectorTextView = findViewById(R.id.movie_director);
        TextView movieGenresTextView = findViewById(R.id.genres_list_content);
        TextView movieStarsTextView = findViewById(R.id.stars_list_content);

        movieTitleTextView.setText("Movie1 (2020)");
        movieRatingTextView.setText(new DecimalFormat("#.0").format(9.5));
        movieDirectorTextView.setText("Director1");
        movieGenresTextView.setText(String.join("\n", genres));
        movieStarsTextView.setText(String.join("\n", stars));
    }
}
