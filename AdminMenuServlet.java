package com.smartcanteen.servlet;

import com.smartcanteen.db.DBConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

/**
 * AdminMenuServlet.java
 * ----------------------
 * Admin-only CRUD operations for the `menu` table.
 *
 * GET  /adminMenu               – list all menu items as JSON
 * POST /adminMenu?action=add    – add a new item
 * POST /adminMenu?action=update – update an existing item
 * POST /adminMenu?action=delete – delete an item
 * POST /adminMenu?action=toggle – toggle availability (0 ↔ 1)
 *
 * URL mapping : /adminMenu
 */
@WebServlet("/adminMenu")
public class AdminMenuServlet extends HttpServlet {

    // ── Admin session guard ────────────────────────────────────────
    private boolean isAdmin(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null
                || !"admin".equals(session.getAttribute("userRole"))) {
            res.sendRedirect("pages/index.html?error=unauthorized");
            return false;
        }
        return true;
    }

    // ── GET – fetch all items (for admin table) ───────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        if (!isAdmin(req, res)) return;

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        PrintWriter out = res.getWriter();

        String sql = "SELECT item_id, item_name, description, price, "
                   + "category, is_available FROM menu ORDER BY category, item_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                first = false;
                json.append("{")
                    .append("\"itemId\":"      ).append(rs.getInt("item_id")).append(",")
                    .append("\"itemName\":\"" ).append(escape(rs.getString("item_name"))).append("\",")
                    .append("\"description\":\"").append(escape(rs.getString("description"))).append("\",")
                    .append("\"price\":"       ).append(rs.getDouble("price")).append(",")
                    .append("\"category\":\"" ).append(escape(rs.getString("category"))).append("\",")
                    .append("\"available\":"   ).append(rs.getInt("is_available"))
                    .append("}");
            }
            json.append("]");
            out.print(json.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            out.print("[]");
        }
    }

    // ── POST – add / update / delete / toggle ─────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        if (!isAdmin(req, res)) return;

        String action = req.getParameter("action");

        try (Connection conn = DBConnection.getConnection()) {

            switch (action == null ? "" : action) {

                // ── ADD new menu item ──────────────────────────────
                case "add": {
                    String sql = "INSERT INTO menu (item_name, description, price, category, is_available) "
                               + "VALUES (?, ?, ?, ?, 1)";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, req.getParameter("itemName"));
                        ps.setString(2, req.getParameter("description"));
                        ps.setDouble(3, Double.parseDouble(req.getParameter("price")));
                        ps.setString(4, req.getParameter("category"));
                        ps.executeUpdate();
                    }
                    break;
                }

                // ── UPDATE existing item ───────────────────────────
                case "update": {
                    String sql = "UPDATE menu SET item_name=?, description=?, price=?, "
                               + "category=?, is_available=? WHERE item_id=?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, req.getParameter("itemName"));
                        ps.setString(2, req.getParameter("description"));
                        ps.setDouble(3, Double.parseDouble(req.getParameter("price")));
                        ps.setString(4, req.getParameter("category"));
                        ps.setInt(5,    Integer.parseInt(req.getParameter("isAvailable")));
                        ps.setInt(6,    Integer.parseInt(req.getParameter("itemId")));
                        ps.executeUpdate();
                    }
                    break;
                }

                // ── DELETE item ────────────────────────────────────
                case "delete": {
                    String sql = "DELETE FROM menu WHERE item_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, Integer.parseInt(req.getParameter("itemId")));
                        ps.executeUpdate();
                    }
                    break;
                }

                // ── TOGGLE availability ────────────────────────────
                case "toggle": {
                    String sql = "UPDATE menu SET is_available = 1 - is_available "
                               + "WHERE item_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, Integer.parseInt(req.getParameter("itemId")));
                        ps.executeUpdate();
                    }
                    break;
                }

                default:
                    res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
                    return;
            }

            res.sendRedirect("pages/admin_dashboard.html?success=true");

        } catch (SQLException e) {
            e.printStackTrace();
            res.sendRedirect("pages/admin_dashboard.html?error=db");
        }
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
