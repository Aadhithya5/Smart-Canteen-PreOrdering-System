package com.smartcanteen.servlet;

import com.smartcanteen.db.DBConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;

/**
 * LoginServlet.java
 * ------------------
 * Authenticates a user (student or admin) against the users table.
 * Stores user details in the HTTP session on success.
 *
 * URL mapping : /login
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // 1. Collect credentials from the login form
        String email    = req.getParameter("email").trim().toLowerCase();
        String password = req.getParameter("password");

        // 2. Query the database (PreparedStatement prevents SQL injection)
        String sql = "SELECT user_id, full_name, role FROM users "
                   + "WHERE email = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // 3. Authentication successful – create session
                HttpSession session = req.getSession(true);
                session.setAttribute("userId",   rs.getInt("user_id"));
                session.setAttribute("userName", rs.getString("full_name"));
                session.setAttribute("userRole", rs.getString("role"));
                session.setMaxInactiveInterval(30 * 60); // 30-minute timeout

                // 4. Route user based on role
                String role = rs.getString("role");
                if ("admin".equals(role)) {
                    res.sendRedirect("pages/admin_dashboard.html");
                } else {
                    res.sendRedirect("pages/menu.html");
                }
            } else {
                // 5. Invalid credentials
                res.sendRedirect("pages/index.html?error=invalid");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            res.sendRedirect("pages/index.html?error=db");
        }
    }
}
