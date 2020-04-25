import com.google.gson.JsonObject;

public class Customer {
    public final int id;
    public final String firstName;

    public Customer(int id, String firstName) {
        this.id = id;
        this.firstName = firstName;
    }

    public JsonObject toJSON() {
        JsonObject ret = new JsonObject();
        ret.addProperty("id", id);
        ret.addProperty("firstName", firstName);
        return ret;
    }
}
