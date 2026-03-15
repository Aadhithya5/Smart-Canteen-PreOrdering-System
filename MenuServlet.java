package com.smartcanteen.servlet;

import com.smartcanteen.db.DBConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

/**
 * MenuServlet.java
 * -----------------
 * Returns all available menu items as a JSON array.
 * menu.html fetches this endpoint via JavaScript (fetch API) and
 * renders the items dynamically.
 *
 * URL mapping : /menu
 */
@WebServlet("/menu")
public class MenuServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // Session guard – only logged-in users may see the menu
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            res.sendRedirect("pages/index.html?error=session");
            return;
        }

        // Optional category filter (e.g., ?category=Lunch)
        String category = req.getParameter("category");

        StringBuilder sql = new StringBuilder(
            "SELECT item_id, item_name, description, price, category, is_available "
          + "FROM menu WHERE is_available = 1 ");

        if (category != null && !category.isEmpty()) {
            sql.append("AND category = ? ");
        }
        sql.append("ORDER BY category, item_name");

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        PrintWriter out = res.getWriter();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            if (category != null && !category.isEmpty()) {
                ps.setString(1, category);
            }

            ResultSet rs = ps.executeQuery();
            StringBuilder json = new StringBuilder("[");
            boolean first = true;

            while (rs.next()) {
                if (!first) json.append(",");
                first = false;
                // Build a JSON object for each menu item
                json.append("{")
                    .append("\"itemId\":"     ).append(rs.getInt("item_id")).append(",")
                    .append("\"itemName\":\"" ).append(escape(rs.getString("item_name"))).append("\",")
                    .append("\"description\":\"").append(escape(rs.getString("description"))).append("\",")
                    .append("\"price\":"      ).append(rs.getDouble("price")).append(",")
                    .append("\"category\":\"" ).append(escape(rs.getString("category"))).append("\",")
                    .append("\"available\":"  ).append(rs.getBoolean("is_available"))
                    .append("}");
            }
            json.append("]");
            out.print(json.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            out.print("[]");   // return empty array on error
        }
    }

    /** Escapes double-quotes inside strings for safe JSON embedding. */
    private String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
