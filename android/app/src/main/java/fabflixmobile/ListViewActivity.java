package fabflixmobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
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
    private String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview);

        movieTitleInput = findViewById(R.id.movieTitleInput);
        searchButton = findViewById(R.id.searchButton);
        url = "https://10.0.2.2:8443/server_war/api/";


        //this should be retrieved from the database and the backend server
        final ArrayList<Movie> movies = new ArrayList<>();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search(movies);
            }
        });




        MovieListViewAdapter adapter = new MovieListViewAdapter(movies, this);

        ListView listView = findViewById(R.id.list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Movie movie = movies.get(position);
            String message = String.format("Clicked on position: %d, name: %s, %d", position, movie.getName(), movie.getYear());
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

            // TODO: put corresponding movie id in this variable
            String movieId = "tt0126029";

            Intent intent = new Intent(ListViewActivity.this, SingleMovie.class);
            intent.putExtra("movieId", movieId);
            startActivity(intent);
        });
    }



    public void search(ArrayList<Movie> movies) {

        // Use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        //request type is GET
        String movieapi = String.format("movies?title=%1$s&year=null&director=null&star=null&genre=null&alnum=null&sort=null&page=null&display=null&fulltext=fulltextsearch",
                movieTitleInput.getText().toString());

        final StringRequest searchRequest = new StringRequest(Request.Method.GET, url + movieapi, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //TODO should parse the json response to redirect to appropriate functions.


                try {
                    JSONObject responseJson = new JSONObject(response);
                    JSONArray moviesArray = new JSONArray(responseJson.getString("movies"));
                    for (int i = 0 ; i < moviesArray.length(); i++) {
                        JSONObject movie = moviesArray.getJSONObject(i);
                        String movieTitle = movie.getString("movieTitle");
                        String movieYear = movie.getString("movieYear");
                        String movieDirector = movie.getString("movieDirector");
                        String movieGenres = movie.getString("movieGenres");
                        String movieStars = movie.getString("movieStars");
                        Log.d("responseJson", movieTitle);
                        movies.add(new Movie(movieTitle, Short.valueOf(movieYear)));
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