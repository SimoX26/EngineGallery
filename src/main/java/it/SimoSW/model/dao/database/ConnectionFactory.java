package it.SimoSW.model.dao.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    static final String DB_URL_PROP = "enginegallery.db.url";
    static final String DB_USER_PROP = "enginegallery.db.user";
    static final String DB_PASSWORD_PROP = "enginegallery.db.password";
    static final String DB_URL_ENV = "ENGINE_GALLERY_DB_URL";
    static final String DB_USER_ENV = "ENGINE_GALLERY_DB_USER";
    static final String DB_PASSWORD_ENV = "ENGINE_GALLERY_DB_PASSWORD";

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

        this.dbUrl = requireConfig("database URL", DB_URL_PROP, DB_URL_ENV);
        this.dbUser = requireConfig("database username", DB_USER_PROP, DB_USER_ENV);
        this.dbPassword = requireConfig("database password", DB_PASSWORD_PROP, DB_PASSWORD_ENV);
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

    private String requireConfig(String label, String systemProperty, String envVar) {
        String value = System.getProperty(systemProperty);
        if (value == null || value.isBlank()) {
            value = System.getenv(envVar);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing required " + label + " configuration. Set system property '" + systemProperty
                            + "' or environment variable '" + envVar + "'."
            );
        }
        return value.trim();
    }
}
