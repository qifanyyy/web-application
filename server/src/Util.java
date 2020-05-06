import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;

public class Util {
    public static JsonObject exception2Json(Exception e) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("status", "failed");
        jsonObject.addProperty("errorMessage", e.getMessage());
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        jsonObject.addProperty("stackTrace", stringWriter.toString());
        return jsonObject;
    }

    public static JsonObject makeGeneralErrorJsonObject(String errorMessage) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("status", "failed");
        jsonObject.addProperty("errorMessage", errorMessage);
        return jsonObject;
    }

    public static final String SITE_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    public static void verifyRecaptcha(String gRecaptchaResponse) throws Exception {
        if (gRecaptchaResponse == null || gRecaptchaResponse.length() == 0) {
            throw new Exception("recaptcha verification failed: gRecaptchaResponse is null of empty");
        }

        URL verifyUrl = new URL(SITE_VERIFY_URL);

        // Open Connection to URL
        HttpsURLConnection conn = (HttpsURLConnection) verifyUrl.openConnection();

        // Add Request Header
        conn.setRequestMethod("POST");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        // Data will be sent to the server.
        String postParams = "secret=" + RecaptchaConstants.SECRET_KEY + "&response=" + gRecaptchaResponse;

        // Send Request
        conn.setDoOutput(true);

        // Get the output stream of Connection
        // Write data in this stream, which means to send data to Server.
        OutputStream outStream = conn.getOutputStream();
        outStream.write(postParams.getBytes());
        outStream.flush();
        outStream.close();

        // Response code return from server.
        int responseCode = conn.getResponseCode();

        // Get the InputStream from Connection to read data sent from the server.
        InputStream inputStream = conn.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        JsonObject jsonObject = new Gson().fromJson(inputStreamReader, JsonObject.class);
        inputStreamReader.close();

        if (!jsonObject.get("success").getAsBoolean()) {
            throw new Exception("recaptcha verification failed: response is " + jsonObject.toString());
        }
    }
}


