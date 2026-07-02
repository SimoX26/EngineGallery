package it.SimoSW.model.dao.database;

import it.SimoSW.model.HydraulicTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseHydraulicTestDAOTest {

    private static final Driver TEST_DRIVER = new HydraulicTestDriver();

    static {
        try {
            DriverManager.registerDriver(TEST_DRIVER);
        } catch (SQLException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        System.setProperty(ConnectionFactory.DB_URL_PROP, "jdbc:testdb:hydraulic");
        System.setProperty(ConnectionFactory.DB_USER_PROP, "valid-user");
        System.setProperty(ConnectionFactory.DB_PASSWORD_PROP, "valid-password");
        resetSingleton();
    }

    @AfterAll
    static void unregisterDriver() throws SQLException {
        DriverManager.deregisterDriver(TEST_DRIVER);
    }

    @Test
    void findAllSkipsInvalidRowsAndKeepsValidHydraulicTests() throws Exception {
        System.setProperty(ConnectionFactory.DB_URL_PROP, "jdbc:testdb:hydraulic");
        System.setProperty(ConnectionFactory.DB_USER_PROP, "valid-user");
        System.setProperty(ConnectionFactory.DB_PASSWORD_PROP, "valid-password");
        resetSingleton();

        DatabaseHydraulicTestDAO dao = new DatabaseHydraulicTestDAO();
        List<HydraulicTest> tests = dao.findAll();

        assertEquals(1, tests.size());
        assertEquals(1L, tests.get(0).getId());
        assertEquals("Cliente valido", tests.get(0).getCustomerName());
    }

    @Test
    void findByIdReturnsEmptyWhenRowIsInvalid() throws Exception {
        System.setProperty(ConnectionFactory.DB_URL_PROP, "jdbc:testdb:hydraulic");
        System.setProperty(ConnectionFactory.DB_USER_PROP, "valid-user");
        System.setProperty(ConnectionFactory.DB_PASSWORD_PROP, "valid-password");
        resetSingleton();

        DatabaseHydraulicTestDAO dao = new DatabaseHydraulicTestDAO();
        Optional<HydraulicTest> test = dao.findById(2L);

        assertTrue(test.isEmpty());
    }

    private static void resetSingleton() throws Exception {
        Field instanceField = ConnectionFactory.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    private static final class HydraulicTestDriver implements Driver {
        private static final List<Map<String, Object>> FIND_ALL_ROWS = List.of(
                row(1L, "Cliente valido", "MTR-001", "video1.mp4", LocalDate.of(2026, 6, 1), "ok"),
                row(2L, "Cliente rotto", "MTR-002", "video2.mp4", null, "broken")
        );

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
                        if ("prepareStatement".equals(name)) {
                            String sql = String.valueOf(args[0]);
                            return createPreparedStatement(sql);
                        }
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
                        return primitiveDefault(method.getReturnType());
                    }
            );
        }

        private PreparedStatement createPreparedStatement(String sql) {
            class State {
                Long id;
            }
            State state = new State();
            return (PreparedStatement) Proxy.newProxyInstance(
                    PreparedStatement.class.getClassLoader(),
                    new Class[]{PreparedStatement.class},
                    (proxy, method, args) -> {
                        String name = method.getName();
                        if ("setLong".equals(name)) {
                            state.id = (Long) args[1];
                            return null;
                        }
                        if ("executeQuery".equals(name)) {
                            List<Map<String, Object>> rows = sql.contains("WHERE id = ?")
                                    ? FIND_ALL_ROWS.stream().filter(row -> row.get("id").equals(state.id)).toList()
                                    : FIND_ALL_ROWS;
                            return createResultSet(rows);
                        }
                        if ("close".equals(name)) {
                            return null;
                        }
                        if ("unwrap".equals(name)) {
                            return null;
                        }
                        if ("isWrapperFor".equals(name)) {
                            return false;
                        }
                        return primitiveDefault(method.getReturnType());
                    }
            );
        }

        private ResultSet createResultSet(List<Map<String, Object>> rows) {
            class Cursor {
                int index = -1;
            }
            Cursor cursor = new Cursor();
            return (ResultSet) Proxy.newProxyInstance(
                    ResultSet.class.getClassLoader(),
                    new Class[]{ResultSet.class},
                    (proxy, method, args) -> {
                        String name = method.getName();
                        if ("next".equals(name)) {
                            cursor.index += 1;
                            return cursor.index < rows.size();
                        }
                        if ("getLong".equals(name)) {
                            return ((Number) rows.get(cursor.index).get(String.valueOf(args[0]))).longValue();
                        }
                        if ("getString".equals(name)) {
                            Object value = rows.get(cursor.index).get(String.valueOf(args[0]));
                            return value != null ? String.valueOf(value) : null;
                        }
                        if ("getDate".equals(name)) {
                            LocalDate value = (LocalDate) rows.get(cursor.index).get(String.valueOf(args[0]));
                            return value != null ? Date.valueOf(value) : null;
                        }
                        if ("getTimestamp".equals(name)) {
                            LocalDateTime value = (LocalDateTime) rows.get(cursor.index).get(String.valueOf(args[0]));
                            return value != null ? Timestamp.valueOf(value) : null;
                        }
                        if ("close".equals(name)) {
                            return null;
                        }
                        if ("wasNull".equals(name)) {
                            return false;
                        }
                        if ("unwrap".equals(name)) {
                            return null;
                        }
                        if ("isWrapperFor".equals(name)) {
                            return false;
                        }
                        return primitiveDefault(method.getReturnType());
                    }
            );
        }

        @Override
        public boolean acceptsURL(String url) {
            return "jdbc:testdb:hydraulic".equals(url);
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

        private static Map<String, Object> row(long id,
                                               String customerName,
                                               String engineCode,
                                               String videoUrl,
                                               LocalDate testDate,
                                               String notes) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", id);
            row.put("customer_name", customerName);
            row.put("engine_code", engineCode);
            row.put("video_url", videoUrl);
            row.put("test_date", testDate);
            row.put("notes", notes);
            row.put("created_at", LocalDateTime.of(2026, 6, 1, 10, 0));
            return row;
        }
    }

    private static Object primitiveDefault(Class<?> type) {
        if (type == boolean.class) {
            return false;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0f;
        }
        if (type == double.class) {
            return 0d;
        }
        return null;
    }
}
