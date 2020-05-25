package fabflixmobile;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login extends ActionBarActivity {

    private EditText username;
    private EditText password;
    private TextView message;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean("EXIT", false)) {
            finish();
        }

        super.onCreate(savedInstanceState);
        // upon creation, inflate and initialize the layout
        setContentView(R.layout.login);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        message = findViewById(R.id.message);
        Button loginButton = findViewById(R.id.login);

        // In Android, localhost is the address of the device or the emulator.
        // To connect to your machine, you need to use the below IP address

        url = "https://10.0.2.2:8443/api/";

        //assign a listener to call a function to handle the user request when clicking a button
        loginButton.setOnClickListener(view -> {
            if (username.getText().toString().trim().length() == 0 ||
                    password.getText().toString().trim().length() == 0) {
                message.setText("Please enter username and password");
                return;
            }
            login();
        });
    }

    public void login() {
        message.setText("Logging in...");
        // Use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        //request type is POST
        final StringRequest loginRequest = new StringRequest(Request.Method.POST, url + "login", response -> {
            try {
                JSONObject responseJson = new JSONObject(response);
                Log.d("response", response);
                Log.d("responseJson", responseJson.getString("status"));
                if (responseJson.getString("status").equals("success")) {
                    Log.d("login.success", response);
                    //initialize the activity(page)/destination
                    Intent listPage = new Intent(Login.this, ListViewActivity.class);
                    //without starting the activity/page, nothing would happen
                    startActivity(listPage);
                }
                else {
                    message.setText(responseJson.getString("message"));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> {
            // error
            Log.e("login.error", error.toString());
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Post request form data
                final Map<String, String> params = new HashMap<>();
                params.put("email", username.getText().toString());
                params.put("password", password.getText().toString());
                params.put("type", "customer");
                return params;
            }
        };

        // !important: queue.add is where the login request is actually sent
        queue.add(loginRequest);
    }
}