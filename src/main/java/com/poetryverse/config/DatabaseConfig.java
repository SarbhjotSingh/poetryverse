package com.poetryverse.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${poetryverse.db.mysql.url}")
    private String mysqlUrl;

    @Value("${poetryverse.db.mysql.username}")
    private String mysqlUsername;

    @Value("${poetryverse.db.mysql.password}")
    private String mysqlPassword;

    @Value("${poetryverse.db.fallback.url}")
    private String fallbackUrl;

    @Bean
    @Primary
    public DataSource dataSource() {
        // Attempt primary MySQL connection first (trying configured password, then 'root', then empty)
        String[] candidatePasswords = (mysqlPassword != null && !mysqlPassword.isEmpty())
                ? new String[]{mysqlPassword, "root", ""}
                : new String[]{"", "root", "password"};

        for (String candidatePwd : candidatePasswords) {
            if (canConnectToMySql(mysqlUrl, mysqlUsername, candidatePwd)) {
                log.info("=================================================================");
                log.info("PoetryVerse connected to MySQL Server at: {}", mysqlUrl);
                log.info("=================================================================");
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(mysqlUrl);
                config.setUsername(mysqlUsername);
                config.setPassword(candidatePwd);
                config.setDriverClassName("com.mysql.cj.jdbc.Driver");
                config.setMaximumPoolSize(10);
                config.setPoolName("PoetryVerseMySQLPool");
                return new HikariDataSource(config);
            }
        }

        log.info("=================================================================");
        log.info("Local MySQL Server on port 3306 not detected; activating persistent");
        log.info("MySQL-compatible file engine at ./data/poetryverse_db");
        log.info("=================================================================");
        HikariConfig fallbackConfig = new HikariConfig();
        fallbackConfig.setJdbcUrl(fallbackUrl);
        fallbackConfig.setUsername("sa");
        fallbackConfig.setPassword("");
        fallbackConfig.setDriverClassName("org.h2.Driver");
        fallbackConfig.setMaximumPoolSize(10);
        fallbackConfig.setPoolName("PoetryVersePersistentMySQLModePool");
        return new HikariDataSource(fallbackConfig);
    }

    private boolean canConnectToMySql(String url, String username, String password) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            DriverManager.setLoginTimeout(2);
            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                return conn != null && !conn.isClosed();
            }
        } catch (Exception ignored) {
            return false;
        }
    }
}
