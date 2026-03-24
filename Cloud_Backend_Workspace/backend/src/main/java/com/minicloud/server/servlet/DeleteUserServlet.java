// Check this very first line! Make sure it exactly matches your folder structure.
package com.minicloud.server.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

// This listens specifically for the delete command
@WebServlet(name = "DeleteUserServlet", urlPatterns = "/admin/delete")
public class DeleteUserServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/cloud_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "7317355"; 

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. Grab the ID sent from the HTML button
        String idString = req.getParameter("id");

        if (idString != null && !idString.isEmpty()) {
            int userId = Integer.parseInt(idString);

            // 2. Use a PreparedStatement to securely delete the user
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                
                pstmt.setInt(1, userId);
                pstmt.executeUpdate(); // Executes the deletion in MySQL
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 3. Instantly refresh the page so the admin sees the updated table
        resp.sendRedirect("/admin");
    }
}