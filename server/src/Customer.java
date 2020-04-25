import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public class Customer {
    public final int id;
    public final String firstName;

    public Customer(int id, String firstName) {
        this.id = id;
        this.firstName = firstName;
    }

    public static JsonElement toJSON(Customer customer) {
        if (customer == null) return JsonNull.INSTANCE;
        JsonObject ret = new JsonObject();
        ret.addProperty("id", customer.id);
        ret.addProperty("firstName", customer.id);
        return ret;
    }
}
