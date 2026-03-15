package com.smartcanteen.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection.java
 * -----------------
 * Singleton-style utility class that creates and returns a JDBC
 * connection to the smart_canteen MySQL database.
 *
 * Change DB_URL, DB_USER, and DB_PASS to match your local setup.
 */
public class DBConnection {

    // ── Connection parameters ──────────────────────────────────────
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/smart_canteen"
                                        + "?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";       // your MySQL username
    private static final String DB_PASS = "root";       // your MySQL password
    // ──────────────────────────────────────────────────────────────

    /**
     * Returns a new Connection object.
     * The caller is responsible for closing it (use try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. "
                    + "Add mysql-connector-java.jar to WEB-INF/lib.", e);
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }
}
