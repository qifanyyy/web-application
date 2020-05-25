package fabflixmobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import org.json.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ListViewActivity extends Activity {

    private EditText movieTitleInput;
    private String url;
    private int page = 1;
    private Switch fuzzySwitch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview);

        movieTitleInput = findViewById(R.id.movieTitleInput);
        Button searchButton = findViewById(R.id.searchButton);
        Button previousButton = findViewById(R.id.perviousButton);
        Button nextButton = findViewById(R.id.nextButton);
        fuzzySwitch = findViewById(R.id.fuzzySwitch);
        url = "https://10.0.2.2:8443/api/";


        //this should be retrieved from the database and the backend server
        final ArrayList<Movie> movies = new ArrayList<>();

        MovieListViewAdapter adapter = new MovieListViewAdapter(movies, this);

        ListView listView = findViewById(R.id.list);
        listView.setAdapter(adapter);

        searchButton.setOnClickListener(view -> {
            if (movieTitleInput.getText().toString().trim().length() > 0)
                search(movies, adapter);
        });

        previousButton.setOnClickListener(view -> {
            if (page <= 1 || movies.isEmpty()) return;
            page -= 1;
            search(movies, adapter);
        });

        nextButton.setOnClickListener(view -> {
            if (movies.isEmpty()) return;
            page += 1;
            search(movies, adapter);
        });


        listView.setOnItemClickListener((parent, view, position, id) -> {
            Movie movie = movies.get(position);
            String movieId = movie.getId();

            Intent intent = new Intent(ListViewActivity.this, SingleMovie.class);
            intent.putExtra("movieId", movieId);
            startActivity(intent);
        });
    }

    public void search(ArrayList<Movie> movies, MovieListViewAdapter adapter) {
        // Use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        //request type is GET
        String display = "20";
        String fulltext = "fulltextsearch";
        String title = movieTitleInput.getText().toString();
        String fuzzy = "Fuzzyoff";

        if (fuzzySwitch.isChecked()) fuzzy = "Fuzzyon";

        String movieapi;
        try {
            movieapi = String.format(
                    "movies?title=%1$s&year=null&director=null&star=null&genre=null&alnum=null&sort=6&" +
                    "page=%2$s&display=%3$s&fulltext=%4$s&fuzzy=%5$s&manualPage=",
                    URLEncoder.encode(title, "utf-8") , page, display, fulltext, fuzzy
            );
        } catch (UnsupportedEncodingException e) {
            Log.e("search.movieapi:UnsupportedEncodingException", e.toString());
            movieapi = String.format(
                    "movies?title=%1$s&year=null&director=null&star=null&genre=null&alnum=null&sort=6&" +
                    "page=%2$s&display=%3$s&fulltext=%4$s&fuzzy=%5$s&manualPage=",
                    title, page, display, fulltext, fuzzy
            );
        }

        String finalUrl = url + movieapi;
        Log.d("movieList finalURL", finalUrl);

        final StringRequest searchRequest = new StringRequest(Request.Method.GET, finalUrl, response -> {
            try {
                JSONObject responseJson = new JSONObject(response);
                JSONArray moviesArray = responseJson.getJSONArray("movies");
                page = Integer.parseInt(responseJson.getJSONObject("page").getString("page"));

                if (moviesArray.length() == 0) {
                    if (page > 1) page -= 1;
                    return;
                }

                movies.clear();
                for (int i = 0 ; i < moviesArray.length(); i++) {
                    JSONObject movie = moviesArray.getJSONObject(i);
                    String movieId = movie.getString("movieId");
                    String movieTitle = movie.getString("movieTitle");
                    String movieYear = movie.getString("movieYear");
                    String movieDirector = "Director: " + movie.getString("movieDirector");

                    StringBuilder movieGenres = new StringBuilder("Genres: ");
                    StringBuilder movieStars = new StringBuilder("Stars: ");

                    JSONArray GenresArray = movie.getJSONArray("movieGenres");
                        for (int j = 0 ; j < GenresArray.length(); j++) {
                            movieGenres.append(GenresArray.getString(j));
                            if (j != GenresArray.length() - 1) movieGenres.append(", ");
                        }

                    JSONArray StarsArray = movie.getJSONArray("movieStars");
                    for (int j = 0 ; j < StarsArray.length(); j++) {
                        movieStars.append(StarsArray.getJSONObject(j).getString("starName"));
                        if (j != StarsArray.length() - 1) movieStars.append(", ");
                    }

                    movies.add(new Movie(movieId, movieTitle, Short.parseShort(movieYear), movieDirector, movieGenres.toString(), movieStars.toString()));
                }
                adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            // error
            Log.e("search.error", error.toString());
            Log.e("search.error", new String(error.networkResponse.data, StandardCharsets.UTF_8));
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Post request form data
                final Map<String, String> params = new HashMap<>();
                params.put("title", movieTitleInput.getText().toString());
                return params;
            }
        };

        // !important: queue.add is where the login request is actually sent
        queue.add(searchRequest);
    }
}