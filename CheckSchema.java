import java.sql.*;
import java.util.Properties;
import java.io.FileInputStream;

public class CheckSchema {
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
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getColumns(null, null, "users", "password_hash")) {
                if (rs.next()) {
                    System.out.println("COLUMN: " + rs.getString("COLUMN_NAME"));
                    System.out.println("SIZE: " + rs.getInt("COLUMN_SIZE"));
                    System.out.println("TYPE: " + rs.getString("TYPE_NAME"));
                }
            }
        }
    }
}
