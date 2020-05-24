package fabflixmobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import org.json.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ListViewActivity extends Activity {

    private EditText movieTitleInput;
    private Button searchButton;
    private Button perviousButton;
    private Button nextButton;
    private String url;
    private int page;
    private Switch fuzzySwitch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview);

        movieTitleInput = findViewById(R.id.movieTitleInput);
        searchButton = findViewById(R.id.searchButton);
        perviousButton = findViewById(R.id.perviousButton);
        nextButton = findViewById(R.id.nextButton);
        fuzzySwitch = findViewById(R.id.fuzzySwitch);
        url = "https://10.0.2.2:8443/server_war/api/";


        //this should be retrieved from the database and the backend server
        final ArrayList<Movie> movies = new ArrayList<Movie>();

        MovieListViewAdapter adapter = new MovieListViewAdapter(movies, this);

        ListView listView = findViewById(R.id.list);
        listView.setAdapter(adapter);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                movies.clear();
                adapter.notifyDataSetChanged();
                search(movies, adapter);
            }
        });

        perviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                page -= 1;
                movies.clear();
                adapter.notifyDataSetChanged();
                search(movies, adapter);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                page += 1;
                movies.clear();
                adapter.notifyDataSetChanged();
                search(movies, adapter);
            }
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
        if (page > 1) {
            display = "null";
            fulltext = "null";
            title = "null";
            fuzzy = "null";
        }

        if (fuzzySwitch.isChecked()) fuzzy = "Fuzzyon";


        String movieapi = String.format("movies?title=%1$s&year=null&director=null&star=null&genre=null&alnum=null&sort=null&page=%2$s&display=%3$s&fulltext=%4$s&fuzzy=%5$s",
               title , String.valueOf(page), display, fulltext, fuzzy);


        final StringRequest searchRequest = new StringRequest(Request.Method.GET, url + movieapi, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //TODO should parse the json response to redirect to appropriate functions.


                try {
                    JSONObject responseJson = new JSONObject(response);
                    JSONArray moviesArray = responseJson.getJSONArray("movies");
                    page = Integer.parseInt(responseJson.getJSONObject("page").getString("page"));
                    for (int i = 0 ; i < moviesArray.length(); i++) {
                        JSONObject movie = moviesArray.getJSONObject(i);
                        String movieId = movie.getString("movieId");
                        String movieTitle = movie.getString("movieTitle");
                        String movieYear = movie.getString("movieYear");
                        String movieDirector = "Director: " + movie.getString("movieDirector");

                        String movieGenres = "Genres: ";
                        String movieStars = "Stars: ";

                        JSONArray GenresArray = movie.getJSONArray("movieGenres");
                            for (int j = 0 ; j < GenresArray.length(); j++) {
                                movieGenres += GenresArray.getString(j);
                                if (j != GenresArray.length() - 1) movieGenres += ", ";
                            }

                        JSONArray StarsArray = movie.getJSONArray("movieStars");
                        for (int j = 0 ; j < StarsArray.length(); j++) {
                            movieStars += StarsArray.getJSONObject(j).getString("starName");
                            if (j != StarsArray.length() - 1) movieStars += ", ";
                        }

                        movies.add(new Movie(movieId, movieTitle, Short.valueOf(movieYear), movieDirector, movieGenres, movieStars));
                        adapter.notifyDataSetChanged();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("search.error", error.toString());
                    }
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