import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8"); // Response mime type
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        out.write(CartItem.cartToJSON((Map<String, CartItem>) req.getSession().getAttribute("cart")).toString());
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8"); // Response mime type
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        String movieId = req.getParameter("movieId");
        String movieTitle = req.getParameter("movieTitle");
        if (movieId == null || movieTitle == null) {
            JsonObject ret = new JsonObject();
            ret.addProperty("status", "failed");
            ret.addProperty("errorMessage", "can't find parameter `movieId` or `movieTitle`");
            resp.setStatus(400);
            out.write(ret.toString());
            out.close();
            return;
        }

        String quantity = req.getParameter("quantity");
        String increment = req.getParameter("increment");
        String decrement = req.getParameter("decrement");

        if (isNotValidParameter(quantity, increment, decrement)) {
            JsonObject ret = new JsonObject();
            ret.addProperty("status", "failed");
            ret.addProperty("errorMessage", "invalid params for quantity/increment/decrement");
            resp.setStatus(400);
            out.write(ret.toString());
            out.close();
            return;
        }

        try {
            HttpSession session;
            Map<String, CartItem> cart;
            synchronized (session = req.getSession()) {
                cart = (Map<String, CartItem>) session.getAttribute("cart");
                boolean needSetSessionAttribute = false;
                if (cart == null) {
                    needSetSessionAttribute = true;
                    cart = new HashMap<>();
                }

                CartItem cartItem;
                if (quantity != null) {
                    cartItem = new CartItem(
                            movieId,
                            movieTitle,
                            Integer.parseInt(quantity)
                    );
                } else if (increment != null) {
                    cartItem = cart.getOrDefault(movieId, new CartItem(movieId, movieTitle, 0));
                    cartItem.quantity += Integer.parseInt(increment);
                } else {
                    cartItem = cart.get(movieId);
                    if (cartItem != null) {
                        cartItem.quantity = Math.max(0, cartItem.quantity - Integer.parseInt(decrement));
                    }
                }

                assert cartItem != null;
                modifyCart(cart, cartItem);

                if (needSetSessionAttribute) {
                    session.setAttribute("cart", cart);
                }
            }
            JsonObject ret = new JsonObject();
            ret.addProperty("status", "success");
            out.write(ret.toString());
            resp.setStatus(200);
        } catch(NumberFormatException e) {
            out.write(Util.makeGeneralErrorJsonObject("invalid value for quantity/incr/decr").toString());
            resp.setStatus(400);
        } catch (Exception e) {
            out.write(Util.exception2Json(e).toString());
            resp.setStatus(500);
        }
        out.close();
    }

    private static void modifyCart(Map<String, CartItem> cart, CartItem item) {
        if (cart.containsKey(item.movieId) && item.quantity == 0) {
            cart.remove(item.movieId);
        } else {
            cart.put(item.movieId, item);
        }
    }

    /**
     * Helper function to test if received cart item manipulation parameter is valid
     * @param q value of quantity parameter
     * @param incr value of increment amount parameter
     * @param decr value of decrement amount parameter
     * @return true if and only if only one pointer in `q`, `incr`, and `decr` is not null
     */
    private static boolean isNotValidParameter(String q, String incr, String decr) {
        return (q == null && incr == null && decr == null) ||
                (q != null && incr != null) || (q != null && decr != null) || (incr != null && decr != null);
    }
}
