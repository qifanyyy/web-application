package fabflixmobile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
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
    private Button previousButton;
    private Button nextButton;
    private TextView pageText;
    private ListView listView;

    private static final ColorMatrixColorFilter grayedOutFilter;

    static {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0.0f);
        grayedOutFilter = new ColorMatrixColorFilter(colorMatrix);
    }

    private void updatePageText() {
        pageText.setText(getString(R.string.page_text, String.valueOf(page), 0));
    }

    private static void disableButton(Button button) {
        button.setEnabled(false);
        button.getBackground().setColorFilter(grayedOutFilter);
    }

    private static void enableButton(Button button) {
        button.setEnabled(true);
        button.getBackground().clearColorFilter();
    }

    private void showSuccessToast() {
        Toast toast = Toast.makeText(this, "Request succeeded", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 300);
        toast.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview);

        movieTitleInput = findViewById(R.id.movieTitleInput);
        Button searchButton = findViewById(R.id.searchButton);
        previousButton = findViewById(R.id.perviousButton);
        nextButton = findViewById(R.id.nextButton);
        fuzzySwitch = findViewById(R.id.fuzzySwitch);
        pageText = findViewById(R.id.page);

        url = "https://10.0.2.2:8443/api/";

        //this should be retrieved from the database and the backend server
        final ArrayList<Movie> movies = new ArrayList<>();

        MovieListViewAdapter adapter = new MovieListViewAdapter(movies, this);

        listView = findViewById(R.id.list);
        listView.setAdapter(adapter);

        searchButton.setOnClickListener(view -> {
            if (movieTitleInput.getText().toString().trim().length() > 0) {
                page = 1;
                search(movies, adapter);
            }
        });

        disableButton(previousButton);
        previousButton.setOnClickListener(view -> {
            if (page <= 1 || movies.isEmpty()) return;
            page -= 1;
            search(movies, adapter);
        });

        disableButton(nextButton);
        nextButton.setOnClickListener(view -> {
            if (movies.isEmpty()) return;
            page += 1;
            search(movies, adapter);
        });

        updatePageText();

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
            Log.e("search.movieapi:UnsupportedEncodingException", e.toString() + " (check movie title)");
            return;
        }

        String finalUrl = url + movieapi;
        Log.d("movieList finalURL", finalUrl);

        final StringRequest searchRequest = new StringRequest(Request.Method.GET, finalUrl, response -> {
            enableButton(nextButton);
            enableButton(previousButton);

            try {
                JSONObject responseJson = new JSONObject(response);
                JSONArray moviesArray = responseJson.getJSONArray("movies");
                page = Integer.parseInt(responseJson.getJSONObject("page").getString("page"));

                if (moviesArray.length() == 0) {
                    if (page > 1) page -= 1;
                    updatePageText();
                    disableButton(nextButton);
                    if (page == 1) {
                        disableButton(previousButton);
                        movies.clear();
                        adapter.notifyDataSetChanged();
                    }
                    showSuccessToast();
                    return;
                }

                if (moviesArray.length() < 20) {
                    disableButton(nextButton);
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
                updatePageText();
                if (page == 1)
                    disableButton(previousButton);
                listView.getHandler().post(() -> listView.setSelectionAfterHeaderView());
                showSuccessToast();
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