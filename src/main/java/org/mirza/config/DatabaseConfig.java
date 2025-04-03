package org.mirza.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.mirza.util.DatabaseUtil;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

@Slf4j
public class DatabaseConfig {
    private static HikariDataSource datasource;

    static {

    }

    private static void initializeDataSource() {
        Properties props = loadProperties();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(props.getProperty("db.password"));
        config.setDriverClassName(props.getProperty("db.driverClassName"));

        // Hikari CP Settings
        config.setMaximumPoolSize(Integer.parseInt(props.getProperty("hikari.maximumPoolSize", "10")));
        config.setConnectionTimeout(Long.parseLong(props.getProperty("hikari.connectionTimeout", "30000")));
        config.setIdleTimeout(Long.parseLong(props.getProperty("hikari.idleTimeout", "600000")));
        config.setMaxLifetime(Long.parseLong(props.getProperty("hikari.maxLifetime", "1800000")));
        config.setAutoCommit(false);
        config.setConnectionTestQuery("SELECT 1");
        config.setLeakDetectionThreshold(5000);  // Log warning if a connection is open for >5s

        // Additional HikariCP settings
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        datasource = new HikariDataSource(config);
        log.info("Database Connection pool initialized successfully");
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream inputStream = DatabaseUtil.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (inputStream != null) {
                props.load(inputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return props;
    }

    public static Connection getConnection() throws SQLException {
        return datasource.getConnection();
    }

    public static DataSource getDataSource() {
        return datasource;
    }

    public static void closeDataSource() {
        if (datasource != null && !datasource.isClosed()) {
            datasource.close();
            log.info("Database Connection pool closed successfully");
        }
    }
}
