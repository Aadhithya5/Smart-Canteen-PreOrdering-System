package com.smartcanteen.servlet;

import com.smartcanteen.db.DBConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

/**
 * OrderStatusServlet.java
 * ------------------------
 * GET  /orderStatus          – returns all orders (admin) or own orders (student) as JSON
 * POST /orderStatus          – admin updates order status
 *
 * Order lifecycle: Pending → Preparing → Ready → Completed
 *
 * URL mapping : /orderStatus
 */
@WebServlet("/orderStatus")
public class OrderStatusServlet extends HttpServlet {

    // ── GET – fetch orders ────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            res.sendRedirect("pages/index.html?error=session");
            return;
        }

        String role   = (String) session.getAttribute("userRole");
        int    userId = (int)    session.getAttribute("userId");

        String sql;
        boolean isAdmin = "admin".equals(role);

        if (isAdmin) {
            // Admin sees all orders with student name
            sql = "SELECT o.order_id, u.full_name, o.total_amount, o.status, "
                + "o.order_time FROM orders o "
                + "JOIN users u ON o.user_id = u.user_id "
                + "ORDER BY o.order_time DESC";
        } else {
            // Student sees only their own orders
            sql = "SELECT o.order_id, u.full_name, o.total_amount, o.status, "
                + "o.order_time FROM orders o "
                + "JOIN users u ON o.user_id = u.user_id "
                + "WHERE o.user_id = ? "
                + "ORDER BY o.order_time DESC";
        }

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        PrintWriter out = res.getWriter();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (!isAdmin) ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();
            StringBuilder json = new StringBuilder("[");
            boolean first = true;

            while (rs.next()) {
                if (!first) json.append(",");
                first = false;
                json.append("{")
                    .append("\"orderId\":"   ).append(rs.getInt("order_id")).append(",")
                    .append("\"student\":\"" ).append(rs.getString("full_name")).append("\",")
                    .append("\"total\":"     ).append(rs.getDouble("total_amount")).append(",")
                    .append("\"status\":\"" ).append(rs.getString("status")).append("\",")
                    .append("\"time\":\"").append(rs.getTimestamp("order_time")).append("\"")
                    .append("}");
            }
            json.append("]");
            out.print(json.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            out.print("[]");
        }
    }

    // ── POST – admin updates status ───────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || !"admin".equals(session.getAttribute("userRole"))) {
            res.sendRedirect("pages/index.html?error=unauthorized");
            return;
        }

        int    orderId   = Integer.parseInt(req.getParameter("orderId"));
        String newStatus = req.getParameter("status");

        // Validate allowed statuses
        if (!newStatus.matches("Pending|Preparing|Ready|Completed|Cancelled")) {
            res.sendRedirect("pages/admin_dashboard.html?error=badstatus");
            return;
        }

        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2,    orderId);
            ps.executeUpdate();

            res.sendRedirect("pages/admin_dashboard.html?success=status");

        } catch (SQLException e) {
            e.printStackTrace();
            res.sendRedirect("pages/admin_dashboard.html?error=db");
        }
    }
}
