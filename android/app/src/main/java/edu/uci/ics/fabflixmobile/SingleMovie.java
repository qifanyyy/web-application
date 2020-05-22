package edu.uci.ics.fabflixmobile;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class SingleMovie extends ActionBarActivity {
    final List<String> genres = new ArrayList<>();
    final List<String> stars = new ArrayList<>();
    ArrayAdapter<String> genresAdapter;
    ArrayAdapter<String> starsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_movie);

        genres.add("g1");
        genres.add("g2");

        stars.add("s1");
        stars.add("s2");

        genresAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, genres);
        starsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, stars);

        ListView genreList = findViewById(R.id.movie_genre_list);
        ListView starList = findViewById(R.id.movie_star_list);

        genreList.setAdapter(genresAdapter);
        starList.setAdapter(starsAdapter);
    }
}
