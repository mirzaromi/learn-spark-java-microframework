package org.mirza.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class DatabaseUtil {
    private static final String URL = "jdbc:postgresql://localhost:5433/my_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(URL);
        config.setUsername(USER);
        config.setPassword(PASSWORD);
        config.setMaximumPoolSize(10);  // Max connections
        config.setMinimumIdle(2);        // Min idle connections
        config.setIdleTimeout(30000);    // Close idle connections after 30s
        config.setMaxLifetime(600000);   // Max connection lifetime
        config.setConnectionTimeout(5000); // Max wait time for a connection
        config.setAutoCommit(false);
        config.setConnectionTestQuery("SELECT 1");
        config.setLeakDetectionThreshold(5000);  // Log warning if a connection is open for >5s

        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void closeDataSource() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
