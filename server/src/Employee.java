import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public class Employee {
    public final String email;
    public final String fullName;

    public Employee(String email, String fullName) {
        this.email = email;
        this.fullName = fullName;
    }

    public static JsonElement toJSON(Employee employee) {
        if (employee == null) return JsonNull.INSTANCE;
        JsonObject ret = new JsonObject();
        ret.addProperty("email", employee.email);
        ret.addProperty("fullName", employee.fullName);
        return ret;
    }
}
