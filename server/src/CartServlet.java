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
        PrintWriter out = resp.getWriter();
        out.write(CartItem.cartToJSON((Map<String, CartItem>) req.getSession().getAttribute("cart")).toString());
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PrintWriter out = resp.getWriter();
        try {
            CartItem cartItem = new CartItem(
                    req.getParameter("movieId"),
                    req.getParameter("movieTitle"),
                    Integer.parseInt(req.getParameter("quantity"))
            );
            HttpSession session;
            Map<String, CartItem> cart;
            synchronized (session = req.getSession()) {
                cart = (Map<String, CartItem>) session.getAttribute("cart");
                modifyCart(cart, cartItem, session);
            }
            JsonObject ret = new JsonObject();
            ret.addProperty("status", "success");
            out.write(ret.toString());
            resp.setStatus(200);
        } catch (Exception e) {
            out.write(Util.exception2Json(e).toString());
            resp.setStatus(500);
        }
        out.close();
    }

    private void modifyCart(Map<String, CartItem> cart, CartItem item, HttpSession session) {
        boolean needSetSessionAttribute = false;
        if (cart == null) {
            needSetSessionAttribute = true;
            cart = new HashMap<>();
        }

        if (cart.containsKey(item.movieId) && item.quantity == 0) {
            cart.remove(item.movieId);
        } else {
            cart.put(item.movieId, item);
        }

        if (needSetSessionAttribute) {
            session.setAttribute("cart", cart);
        }
    }
}
