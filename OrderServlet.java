package com.smartcanteen.servlet;

import com.smartcanteen.db.DBConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

/**
 * OrderServlet.java
 * ------------------
 * Converts the session cart into a persisted order in the database.
 * Steps:
 *   1. Read cart from session
 *   2. Insert a row into `orders`
 *   3. Insert one row per cart item into `order_items`
 *   4. Insert a row into `payments`
 *   5. Clear the session cart
 *   6. Redirect to order_success.html
 *
 * Uses a database TRANSACTION so all inserts succeed or all roll back.
 *
 * URL mapping : /order
 */
@WebServlet("/order")
public class OrderServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // 1. Session guard
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            res.sendRedirect("pages/index.html?error=session");
            return;
        }

        @SuppressWarnings("unchecked")
        Map<Integer, Map<String, Object>> cart =
            (Map<Integer, Map<String, Object>>) session.getAttribute("cart");

        if (cart == null || cart.isEmpty()) {
            res.sendRedirect("pages/cart.html?error=empty");
            return;
        }

        int    userId        = (int) session.getAttribute("userId");
        String paymentMethod = req.getParameter("paymentMethod");
        String notes         = req.getParameter("notes");
        if (paymentMethod == null) paymentMethod = "Cash";

        // 2. Calculate grand total
        double total = 0.0;
        for (Map<String, Object> item : cart.values()) {
            total += (double) item.get("price") * (int) item.get("qty");
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);   // Begin transaction

            // 3. Insert into orders
            String orderSql = "INSERT INTO orders (user_id, total_amount, status, notes) "
                            + "VALUES (?, ?, 'Pending', ?)";
            int orderId;
            try (PreparedStatement ps = conn.prepareStatement(
                    orderSql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, userId);
                ps.setDouble(2, total);
                ps.setString(3, notes);
                ps.executeUpdate();

                ResultSet keys = ps.getGeneratedKeys();
                keys.next();
                orderId = keys.getInt(1);
            }

            // 4. Insert into order_items (one row per cart entry)
            String itemSql = "INSERT INTO order_items (order_id, item_id, quantity, unit_price) "
                           + "VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
                for (Map.Entry<Integer, Map<String, Object>> entry : cart.entrySet()) {
                    ps.setInt(1,    orderId);
                    ps.setInt(2,    entry.getKey());
                    ps.setInt(3,    (int)    entry.getValue().get("qty"));
                    ps.setDouble(4, (double) entry.getValue().get("price"));
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // 5. Insert into payments
            String paySql = "INSERT INTO payments (order_id, amount, payment_method, payment_status) "
                          + "VALUES (?, ?, ?, 'Pending')";
            try (PreparedStatement ps = conn.prepareStatement(paySql)) {
                ps.setInt(1,    orderId);
                ps.setDouble(2, total);
                ps.setString(3, paymentMethod);
                ps.executeUpdate();
            }

            conn.commit();   // Commit transaction

            // 6. Clear cart and store order id in session for confirmation page
            cart.clear();
            session.setAttribute("lastOrderId",     orderId);
            session.setAttribute("lastOrderTotal",  total);

            res.sendRedirect("pages/order_success.html");

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            res.sendRedirect("pages/cart.html?error=order");
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { /* ignore */ }
            }
        }
    }
}
