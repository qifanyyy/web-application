import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import java.util.Map;

public class CartItem {
    public String movieId;
    public int quantity;

    public CartItem(String movieId, int quantity) {
        this.movieId = movieId;
        this.quantity = quantity;
    }

    public static JsonElement toJSON(CartItem cartItem) {
        if (cartItem == null) return JsonNull.INSTANCE;
        JsonObject ret = new JsonObject();
        ret.addProperty("movieId", cartItem.movieId);
        ret.addProperty("quantity", cartItem.quantity);
        return ret;
    }

    public static JsonArray cartToJSON(Map<String, CartItem> cart) {
        JsonArray ret = new JsonArray();
        if (cart != null) {
            cart.forEach((__, movie) -> ret.add(CartItem.toJSON(movie)));
        }
        return ret;
    }
}
