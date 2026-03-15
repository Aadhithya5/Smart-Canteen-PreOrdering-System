package com.smartcanteen.servlet;

import com.smartcanteen.db.DBConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;

/**
 * RegisterServlet.java
 * ---------------------
 * Handles POST requests from register.html.
 * Inserts a new student record into the users table.
 *
 * URL mapping : /register
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // 1. Read form parameters
        String fullName = req.getParameter("fullName").trim();
        String email    = req.getParameter("email").trim().toLowerCase();
        String password = req.getParameter("password");        // hash in production!
        String phone    = req.getParameter("phone").trim();

        // 2. Basic server-side validation
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            res.sendRedirect("pages/register.html?error=empty");
            return;
        }

        // 3. Insert into database using PreparedStatement (prevents SQL injection)
        String sql = "INSERT INTO users (full_name, email, password, phone, role) "
                   + "VALUES (?, ?, ?, ?, 'student')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, password);   // use BCrypt hash in real projects
            ps.setString(4, phone);

            ps.executeUpdate();

            // 4. Redirect to login page on success
            res.sendRedirect("pages/index.html?registered=true");

        } catch (SQLIntegrityConstraintViolationException e) {
            // Duplicate email
            res.sendRedirect("pages/register.html?error=duplicate");
        } catch (SQLException e) {
            e.printStackTrace();
            res.sendRedirect("pages/register.html?error=db");
        }
    }
}
