package seoulbin.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtils {
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:sqlite:src/main/java/seoulbin/database/seoulbin.sqlite3";
        return DriverManager.getConnection(url);
    }
}
