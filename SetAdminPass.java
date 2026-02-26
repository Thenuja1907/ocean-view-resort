import java.sql.*;
import java.util.Properties;
import java.io.FileInputStream;
import at.favre.lib.crypto.bcrypt.BCrypt;

public class SetAdminPass {
    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("src/main/resources/db.properties")) {
            props.load(fis);
        }

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.username");
        String pass = props.getProperty("db.password");

        Class.forName(props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));

        String hash = BCrypt.withDefaults().hashToString(12, "Admin@1234".toCharArray());
        System.out.println("Generated Hash: " + hash);

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            String sql = "UPDATE users SET password_hash = ? WHERE username = 'admin'";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, hash);
                int rows = ps.executeUpdate();
                System.out.println("Updated " + rows + " rows.");
            }
        }
    }
}
