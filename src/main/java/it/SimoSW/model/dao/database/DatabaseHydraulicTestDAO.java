package it.SimoSW.model.dao.database;

import it.SimoSW.model.HydraulicTest;
import it.SimoSW.model.dao.HydraulicTestDAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHydraulicTestDAO implements HydraulicTestDAO {

    private static final String FIND_ALL_SQL = """
        SELECT id, customer_name, engine_code, video_url, test_date, notes, created_at
        FROM hydraulic_tests
        ORDER BY test_date DESC, id DESC
    """;

    private static final String SEARCH_SQL = """
        SELECT id, customer_name, engine_code, video_url, test_date, notes, created_at
        FROM hydraulic_tests
        WHERE customer_name LIKE ?
           OR engine_code LIKE ?
           OR notes LIKE ?
        ORDER BY test_date DESC, id DESC
    """;

    @Override
    public List<HydraulicTest> findAll() {
        List<HydraulicTest> tests = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tests.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero delle prove idrauliche", e);
        }

        return tests;
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

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tests.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore nella ricerca prove idrauliche", e);
        }

        return tests;
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
                createdAtSql != null ? createdAtSql.toLocalDateTime() : null
        );
    }
}
