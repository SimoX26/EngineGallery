package it.SimoSW.model.dao.database;

import it.SimoSW.model.HydraulicTest;
import it.SimoSW.model.dao.HydraulicTestDAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseHydraulicTestDAO implements HydraulicTestDAO {
    private static final Logger LOGGER = Logger.getLogger(DatabaseHydraulicTestDAO.class.getName());

    private static final String INSERT_SQL = """
        INSERT INTO hydraulic_tests
        (customer_name, engine_code, video_url, test_date, notes, created_by)
        VALUES (?, ?, ?, ?, ?, ?)
    """;

    private static final String FIND_ALL_SQL = """
        SELECT id, customer_name, engine_code, video_url, test_date, notes, created_at, created_by
        FROM hydraulic_tests
        ORDER BY test_date DESC, id DESC
    """;

    private static final String UPDATE_SQL = """
        UPDATE hydraulic_tests
        SET customer_name = ?, engine_code = ?, test_date = ?, notes = ?
        WHERE id = ?
    """;

    private static final String DELETE_SQL = "DELETE FROM hydraulic_tests WHERE id = ?";

    private static final String FIND_BY_ID_SQL = """
        SELECT id, customer_name, engine_code, video_url, test_date, notes, created_at, created_by
        FROM hydraulic_tests
        WHERE id = ?
    """;

    private static final String SEARCH_SQL = """
        SELECT id, customer_name, engine_code, video_url, test_date, notes, created_at, created_by
        FROM hydraulic_tests
        WHERE customer_name LIKE ?
           OR engine_code LIKE ?
           OR notes LIKE ?
           OR created_by LIKE ?
        ORDER BY test_date DESC, id DESC
    """;
    private static final String COUNT_BY_TEST_DATE_BETWEEN_SQL = """
        SELECT COUNT(*)
        FROM hydraulic_tests
        WHERE test_date BETWEEN ? AND ?
    """;

    @Override
    public HydraulicTest save(HydraulicTest hydraulicTest) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, hydraulicTest.getCustomerName());
            stmt.setString(2, hydraulicTest.getEngineCode());
            stmt.setString(3, hydraulicTest.getVideoUrl());
            stmt.setDate(4, Date.valueOf(hydraulicTest.getTestDate()));
            stmt.setString(5, hydraulicTest.getNotes());
            stmt.setString(6, hydraulicTest.getCreatedBy());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long generatedId = rs.getLong(1);
                    return new HydraulicTest(
                            generatedId,
                            hydraulicTest.getCustomerName(),
                            hydraulicTest.getEngineCode(),
                            hydraulicTest.getVideoUrl(),
                            hydraulicTest.getTestDate(),
                            hydraulicTest.getNotes(),
                            null,
                            hydraulicTest.getCreatedBy()
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio della prova idraulica", e);
        }

        throw new RuntimeException("ID generato non disponibile dopo il salvataggio della prova idraulica");
    }

    @Override
    public void update(HydraulicTest hydraulicTest) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            stmt.setString(1, hydraulicTest.getCustomerName());
            stmt.setString(2, hydraulicTest.getEngineCode());
            stmt.setDate(3, Date.valueOf(hydraulicTest.getTestDate()));
            stmt.setString(4, hydraulicTest.getNotes());
            stmt.setLong(5, hydraulicTest.getId());
            if (stmt.executeUpdate() == 0) {
                throw new IllegalStateException("Prova idraulica non trovata");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la modifica della prova idraulica", e);
        }
    }

    @Override
    public boolean delete(long id) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'eliminazione della prova idraulica", e);
        }
    }

    @Override
    public List<HydraulicTest> findAll() {
        List<HydraulicTest> tests = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                mapRowSafely(rs).ifPresent(tests::add);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero delle prove idrauliche", e);
        }

        return tests;
    }

    @Override
    public Optional<HydraulicTest> findById(long id) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID_SQL)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowSafely(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero prova idraulica ID: " + id, e);
        }

        return Optional.empty();
    }

    @Override
    public int countByTestDateBetween(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from/to non possono essere null");
        }
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_BY_TEST_DATE_BETWEEN_SQL)) {
            stmt.setDate(1, Date.valueOf(from));
            stmt.setDate(2, Date.valueOf(to));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel conteggio prove idrauliche nel periodo", e);
        }
        return 0;
    }

    @Override
    public List<HydraulicTest> search(String keyword) {
        List<HydraulicTest> tests = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SEARCH_SQL)) {

            String wildcardKeyword = "%" + keyword + "%";
            stmt.setString(1, wildcardKeyword);
            stmt.setString(2, wildcardKeyword);
            stmt.setString(3, wildcardKeyword);
            stmt.setString(4, wildcardKeyword);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    mapRowSafely(rs).ifPresent(tests::add);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore nella ricerca prove idrauliche", e);
        }

        return tests;
    }

    private Optional<HydraulicTest> mapRowSafely(ResultSet rs) throws SQLException {
        try {
            return Optional.of(mapRow(rs));
        } catch (RuntimeException ex) {
            long id = safeGetLong(rs, "id");
            LOGGER.log(Level.WARNING, "Prova idraulica ignorata per dati non validi. ID=" + id, ex);
            return Optional.empty();
        }
    }

    private HydraulicTest mapRow(ResultSet rs) throws SQLException {
        Date testDateSql = rs.getDate("test_date");
        Timestamp createdAtSql = rs.getTimestamp("created_at");

        return new HydraulicTest(
                rs.getLong("id"),
                rs.getString("customer_name"),
                rs.getString("engine_code"),
                rs.getString("video_url"),
                testDateSql != null ? testDateSql.toLocalDate() : null,
                rs.getString("notes"),
                createdAtSql != null ? createdAtSql.toLocalDateTime() : null,
                rs.getString("created_by")
        );
    }

    private static long safeGetLong(ResultSet rs, String column) {
        try {
            return rs.getLong(column);
        } catch (SQLException ex) {
            return -1L;
        }
    }
}
