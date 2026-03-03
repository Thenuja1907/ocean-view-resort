import java.sql.*;
import java.util.Properties;
import java.io.FileInputStream;

public class UserStatus {
    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("src/main/resources/db.properties")) {
            props.load(fis);
        }
        String url = props.getProperty("db.url");
        String user = props.getProperty("db.username");
        String pass = props.getProperty("db.password");
        Class.forName(props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            String sql = "SELECT username, is_active FROM users WHERE username='admin'";
            try (Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) {
                    System.out
                            .println("USER: " + rs.getString("username") + " | ACTIVE: " + rs.getBoolean("is_active"));
                }
            }
        }
    }
}
