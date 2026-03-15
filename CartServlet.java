package com.smartcanteen.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * CartServlet.java
 * -----------------
 * Manages the shopping cart stored in the HTTP session.
 * The cart is a Map<itemId, Map{name, price, qty}> so no DB
 * round-trip is needed until the student places an order.
 *
 * Supported actions (POST parameter "action"):
 *   add    – add one unit of an item
 *   remove – remove one unit of an item
 *   clear  – empty the cart
 *   view   – return cart contents as JSON (GET)
 *
 * URL mapping : /cart
 */
@WebServlet("/cart")
public class CartServlet extends HttpServlet {

    private static final String CART_KEY = "cart";

    // ── GET – return current cart as JSON ─────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            res.sendRedirect("pages/index.html?error=session");
            return;
        }

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        PrintWriter out = res.getWriter();

        @SuppressWarnings("unchecked")
        Map<Integer, Map<String, Object>> cart =
            (Map<Integer, Map<String, Object>>) session.getAttribute(CART_KEY);

        if (cart == null || cart.isEmpty()) {
            out.print("[]");
            return;
        }

        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        double grandTotal = 0.0;

        for (Map.Entry<Integer, Map<String, Object>> entry : cart.entrySet()) {
            if (!first) json.append(",");
            first = false;

            Map<String, Object> item = entry.getValue();
            int    qty      = (int)    item.get("qty");
            double price    = (double) item.get("price");
            double subtotal = qty * price;
            grandTotal += subtotal;

            json.append("{")
                .append("\"itemId\":"    ).append(entry.getKey()).append(",")
                .append("\"itemName\":\"").append(item.get("name")).append("\",")
                .append("\"price\":"     ).append(price).append(",")
                .append("\"qty\":"       ).append(qty).append(",")
                .append("\"subtotal\":"  ).append(subtotal)
                .append("}");
        }
        json.append("]");
        out.print(json.toString());
    }

    // ── POST – add / remove / clear ───────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            res.sendRedirect("pages/index.html?error=session");
            return;
        }

        String action = req.getParameter("action");

        @SuppressWarnings("unchecked")
        Map<Integer, Map<String, Object>> cart =
            (Map<Integer, Map<String, Object>>) session.getAttribute(CART_KEY);

        if (cart == null) {
            cart = new LinkedHashMap<>();
            session.setAttribute(CART_KEY, cart);
        }

        switch (action == null ? "" : action) {

            case "add": {
                int    itemId = Integer.parseInt(req.getParameter("itemId"));
                String name   = req.getParameter("itemName");
                double price  = Double.parseDouble(req.getParameter("price"));

                if (cart.containsKey(itemId)) {
                    cart.get(itemId).put("qty",
                        (int) cart.get(itemId).get("qty") + 1);
                } else {
                    Map<String, Object> details = new HashMap<>();
                    details.put("name",  name);
                    details.put("price", price);
                    details.put("qty",   1);
                    cart.put(itemId, details);
                }
                break;
            }

            case "remove": {
                int itemId = Integer.parseInt(req.getParameter("itemId"));
                if (cart.containsKey(itemId)) {
                    int qty = (int) cart.get(itemId).get("qty");
                    if (qty <= 1) {
                        cart.remove(itemId);
                    } else {
                        cart.get(itemId).put("qty", qty - 1);
                    }
                }
                break;
            }

            case "clear":
                cart.clear();
                break;

            default:
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
                return;
        }

        // Redirect back to cart page after modification
        res.sendRedirect("pages/cart.html");
    }
}
