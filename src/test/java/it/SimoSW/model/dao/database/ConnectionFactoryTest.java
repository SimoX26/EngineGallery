package it.SimoSW.model.dao.database;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectionFactoryTest {

    private static final Driver TEST_DRIVER = new TestDriver();

    static {
        try {
            DriverManager.registerDriver(TEST_DRIVER);
        } catch (SQLException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        System.clearProperty(ConnectionFactory.DB_URL_PROP);
        System.clearProperty(ConnectionFactory.DB_USER_PROP);
        System.clearProperty(ConnectionFactory.DB_PASSWORD_PROP);
        resetSingleton();
    }

    @AfterAll
    static void unregisterDriver() throws SQLException {
        DriverManager.deregisterDriver(TEST_DRIVER);
    }

    @Test
    void missingAllConfigurationFailsFast() throws Exception {
        IllegalStateException ex = assertThrows(IllegalStateException.class, ConnectionFactory::getInstance);
        assertTrue(ex.getMessage().contains(ConnectionFactory.DB_URL_PROP));
    }

    @Test
    void blankConfigurationFailsFast() throws Exception {
        System.setProperty(ConnectionFactory.DB_URL_PROP, "   ");
        System.setProperty(ConnectionFactory.DB_USER_PROP, "user");
        System.setProperty(ConnectionFactory.DB_PASSWORD_PROP, "password");

        IllegalStateException ex = assertThrows(IllegalStateException.class, ConnectionFactory::getInstance);
        assertTrue(ex.getMessage().contains(ConnectionFactory.DB_URL_PROP));
    }

    @Test
    void wrongCredentialsPropagateConnectionFailure() throws Exception {
        System.setProperty(ConnectionFactory.DB_URL_PROP, "jdbc:testdb:ok");
        System.setProperty(ConnectionFactory.DB_USER_PROP, "wrong");
        System.setProperty(ConnectionFactory.DB_PASSWORD_PROP, "creds");

        ConnectionFactory factory = ConnectionFactory.getInstance();
        SQLException ex = assertThrows(SQLException.class, factory::getConnection);
        assertEquals("Invalid database credentials", ex.getMessage());
    }

    @Test
    void validConfigurationEstablishesConnection() throws Exception {
        System.setProperty(ConnectionFactory.DB_URL_PROP, "jdbc:testdb:ok");
        System.setProperty(ConnectionFactory.DB_USER_PROP, "valid-user");
        System.setProperty(ConnectionFactory.DB_PASSWORD_PROP, "valid-password");

        ConnectionFactory factory = ConnectionFactory.getInstance();
        try (Connection connection = factory.getConnection()) {
            assertNotNull(connection);
            assertTrue(connection.isValid(1));
        }
    }

    private static void resetSingleton() throws Exception {
        Field instanceField = ConnectionFactory.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    private static final class TestDriver implements Driver {
        @Override
        public Connection connect(String url, Properties info) throws SQLException {
            if (!acceptsURL(url)) {
                return null;
            }
            String user = info.getProperty("user");
            String password = info.getProperty("password");
            if (!"valid-user".equals(user) || !"valid-password".equals(password)) {
                throw new SQLException("Invalid database credentials");
            }
            return (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class[]{Connection.class},
                    (proxy, method, args) -> {
                        String name = method.getName();
                        if ("close".equals(name)) {
                            return null;
                        }
                        if ("isClosed".equals(name)) {
                            return false;
                        }
                        if ("isValid".equals(name)) {
                            return true;
                        }
                        if ("unwrap".equals(name)) {
                            return null;
                        }
                        if ("isWrapperFor".equals(name)) {
                            return false;
                        }
                        Class<?> returnType = method.getReturnType();
                        if (returnType == boolean.class) {
                            return false;
                        }
                        if (returnType == int.class) {
                            return 0;
                        }
                        if (returnType == long.class) {
                            return 0L;
                        }
                        if (returnType == float.class) {
                            return 0f;
                        }
                        if (returnType == double.class) {
                            return 0d;
                        }
                        return null;
                    }
            );
        }

        @Override
        public boolean acceptsURL(String url) {
            return "jdbc:testdb:ok".equals(url);
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
            return new DriverPropertyInfo[0];
        }

        @Override
        public int getMajorVersion() {
            return 1;
        }

        @Override
        public int getMinorVersion() {
            return 0;
        }

        @Override
        public boolean jdbcCompliant() {
            return false;
        }

        @Override
        public Logger getParentLogger() {
            return Logger.getGlobal();
        }
    }
}
