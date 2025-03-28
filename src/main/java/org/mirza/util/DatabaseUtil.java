package org.mirza.util;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
public class DatabaseUtil {
    private static final String URL = "jdbc:postgresql://localhost:5433/my_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            log.info("Connecting to database...");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }

        log.info("Connected to database");
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null) {
                log.info("Closing connection...");
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
