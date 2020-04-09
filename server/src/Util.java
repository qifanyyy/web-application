import com.google.gson.JsonObject;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Util {
    public static JsonObject exception2Json(Exception e) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("errorMessage", e.getMessage());
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        jsonObject.addProperty("stackTrace", stringWriter.toString());
        return jsonObject;
    }
}
