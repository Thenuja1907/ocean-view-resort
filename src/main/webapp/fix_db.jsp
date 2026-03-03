<%@ page import="com.oceanview.util.DatabaseConnection" %>
    <%@ page import="java.sql.Connection" %>
        <%@ page import="java.sql.Statement" %>
            <% Connection conn=null; try { conn=DatabaseConnection.getInstance().getConnection(); Statement
                stmt=conn.createStatement(); // Change payment_method to VARCHAR(50) to avoid any truncation issues with
                ENUM mismatches stmt.executeUpdate("ALTER TABLE bills MODIFY COLUMN payment_method VARCHAR(50) NULL");
                out.println("<h2>Success: Database schema updated successfully.</h2>");
                out.println("<p>The 'payment_method' column in the 'bills' table has been modified to VARCHAR(50).</p>
                ");
                } catch (Exception e) {
                out.println("<h2>Error updating schema:</h2>");
                out.println("
                <pre>" + e.getMessage() + "</pre>");
                e.printStackTrace();
                } finally {
                if (conn != null) {
                DatabaseConnection.getInstance().releaseConnection(conn);
                }
                }
                %>
                <br>
                <a href="dashboard.html">Return to Dashboard</a>