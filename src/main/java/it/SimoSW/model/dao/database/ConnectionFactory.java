package it.SimoSW.model.dao.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    private static final String DB_URL_PROP = "enginegallery.db.url";
    private static final String DB_USER_PROP = "enginegallery.db.user";
    private static final String DB_PASSWORD_PROP = "enginegallery.db.password";
    private static final String DB_URL_ENV = "ENGINE_GALLERY_DB_URL";
    private static final String DB_USER_ENV = "ENGINE_GALLERY_DB_USER";
    private static final String DB_PASSWORD_ENV = "ENGINE_GALLERY_DB_PASSWORD";
    private static final String DEFAULT_DB_URL = "jdbc:mysql://localhost:3306/engine_gallery?serverTimezone=UTC";
    private static final String DEFAULT_DB_USER = "engine_gallery";
    private static final String DEFAULT_DB_PASSWORD = "engine123";

    private static ConnectionFactory instance;

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    private ConnectionFactory() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }

        this.dbUrl = requireConfig(DB_URL_PROP, DB_URL_ENV);
        this.dbUser = requireConfig(DB_USER_PROP, DB_USER_ENV);
        this.dbPassword = requireConfig(DB_PASSWORD_PROP, DB_PASSWORD_ENV);
    }

    public static ConnectionFactory getInstance() {
        if (instance == null) {
            instance = new ConnectionFactory();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    private String requireConfig(String systemProperty, String envVar) {
        String value = System.getProperty(systemProperty);
        if (value == null || value.isBlank()) {
            value = System.getenv(envVar);
        }
        if (value == null || value.isBlank()) {
            if (DB_URL_PROP.equals(systemProperty)) {
                return DEFAULT_DB_URL;
            }
            if (DB_USER_PROP.equals(systemProperty)) {
                return DEFAULT_DB_USER;
            }
            if (DB_PASSWORD_PROP.equals(systemProperty)) {
                return DEFAULT_DB_PASSWORD;
            }
        }
        return value;
    }
}
