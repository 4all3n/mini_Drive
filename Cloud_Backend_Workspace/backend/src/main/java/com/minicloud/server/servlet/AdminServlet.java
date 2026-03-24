// Check this very first line! Make sure it exactly matches your folder structure.
package com.minicloud.server.servlet; 

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet(name = "AdminServlet", urlPatterns = "/admin")
public class AdminServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/cloud_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "7317355"; 

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        
        int userCount = 0;
        StringBuilder tableRows = new StringBuilder();

        // 1. Fetch ALL users from the database
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, username FROM users")) {
            
            // Loop through every user in the database
            while (rs.next()) {
                userCount++; // Count them as we go
                int id = rs.getInt("id");
                String username = rs.getString("username");
                
                // 2. Build the HTML row with a Delete Form inside it
                tableRows.append("<tr>")
                         .append("<td>").append(id).append("</td>")
                         .append("<td>").append(username).append("</td>")
                         .append("<td>")
                         // This form sends an HTTP POST to our new delete servlet
                         .append("<form action='/admin/delete' method='POST' style='margin:0;'>")
                         .append("<input type='hidden' name='id' value='").append(id).append("'/>")
                         .append("<button type='submit' class='delete-btn'>Delete</button>")
                         .append("</form>")
                         .append("</td>")
                         .append("</tr>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. Inject data into HTML and display
        try (InputStream is = getClass().getResourceAsStream("/admin.html")) {
            if (is == null) {
                out.println("<h1>Error: admin.html not found!</h1>");
                return;
            }
            
            String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            html = html.replace("{{USER_COUNT}}", String.valueOf(userCount));
            html = html.replace("{{USER_TABLE_ROWS}}", tableRows.toString()); // Inject the table rows!
            out.print(html);
        }
    }
}