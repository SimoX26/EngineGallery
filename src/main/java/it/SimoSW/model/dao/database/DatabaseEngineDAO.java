package it.SimoSW.model.dao.database;

import it.SimoSW.model.Engine;
import it.SimoSW.model.EngineStatus;
import it.SimoSW.model.dao.EngineDAO;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseEngineDAO implements EngineDAO {

    /* =====================
       SQL
       ===================== */

    private static final String INSERT_SQL = """
        INSERT INTO engines
        (engine_ref, engine_code, customer_id, status, intake_date, delivery_date, notes)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """;

    private static final String UPDATE_SQL = """
        UPDATE engines
        SET engine_code = ?, status = ?, intake_date = ?, delivery_date = ?, notes = ?, customer_id = ?
        WHERE engine_ref = ?
    """;

    private static final String FIND_BY_ID_SQL =
            "SELECT * FROM engines WHERE id = ?";

    private static final String FIND_BY_REF_SQL =
            "SELECT * FROM engines WHERE engine_ref = ?";

    private static final String FIND_BY_CODE_SQL =
            "SELECT * FROM engines WHERE engine_code = ?";

    private static final String FIND_ALL_SQL =
            "SELECT * FROM engines";

    private static final String FIND_BY_STATUS_SQL =
            "SELECT * FROM engines WHERE status = ?";

    private static final String SEARCH_SQL = """
        SELECT DISTINCT e.*
        FROM engines e
        LEFT JOIN customers c ON e.customer_id = c.id
        WHERE e.engine_ref LIKE ?
           OR e.engine_code LIKE ?
           OR e.notes LIKE ?
           OR c.name LIKE ?
           OR c.company_name LIKE ?
    """;

    private static final String COUNT_BY_STATUS_SQL =
            "SELECT COUNT(*) FROM engines WHERE status = ?";

    private static final String COUNT_IN_WORKSHOP_SQL =
            "SELECT COUNT(*) FROM engines WHERE status IN ('WAITING', 'WORK_IN_PROGRESS', 'READY')";

    private static final String COUNT_DELIVERED_BETWEEN_SQL = """
        SELECT COUNT(*)
        FROM engines
        WHERE status = 'DELIVERED'
          AND delivery_date IS NOT NULL
          AND delivery_date BETWEEN ? AND ?
    """;

    private static final String FIND_BY_CUSTOMER_AND_ENGINE_CODE_SQL = """
        SELECT e.*
        FROM engines e
        JOIN customers c ON e.customer_id = c.id
        WHERE c.name = ?
          AND e.engine_code = ?
    """;


    private static final String GET_NEXT_SEQUENCE_FOR_YEAR = """
        SELECT COALESCE(
            MAX(CAST(SUBSTRING(engine_ref, 10, 5) AS UNSIGNED)),
            0
        ) AS last_seq
        FROM engines
        WHERE engine_ref LIKE CONCAT('RML-', ?, '-%')
        """;



    /* =====================
       CRUD
       ===================== */
    @Override
    public Engine save(Engine engine) {

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     INSERT_SQL,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            stmt.setString(1, engine.getEngineRef());
            stmt.setString(2, engine.getEngineCode());
            stmt.setLong(3, engine.getCustomerId());
            stmt.setString(4, engine.getStatus().name());
            stmt.setDate(5, Date.valueOf(engine.getIntakeDate()));

            if (engine.getDeliveryDate() != null) {
                stmt.setDate(6, Date.valueOf(engine.getDeliveryDate()));
            } else {
                stmt.setNull(6, Types.DATE);
            }

            stmt.setString(7, engine.getNotes());

            stmt.executeUpdate();

            // =========================
            // ID generato
            // =========================
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long generatedId = rs.getLong(1);

                    engine.setId(generatedId);

                    return engine;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio del motore", e);
        }

        throw new RuntimeException(
                "ID generato non disponibile dopo il salvataggio"
        );
    }


    @Override
    public Engine update(Engine engine) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            stmt.setString(1, engine.getEngineCode());
            stmt.setString(2, engine.getStatus().name());
            stmt.setDate(3, Date.valueOf(engine.getIntakeDate()));

            if (engine.getDeliveryDate() != null) {
                stmt.setDate(4, Date.valueOf(engine.getDeliveryDate()));
            } else {
                stmt.setNull(4, Types.DATE);
            }

            stmt.setString(5, engine.getNotes());
            stmt.setLong(6, engine.getCustomerId());

            stmt.setString(7, engine.getEngineRef());

            stmt.executeUpdate();
            return engine;

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiornamento del motore", e);
        }
    }

    @Override
    public boolean delete(String engineRef) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM engines WHERE engine_ref = ?")) {

            stmt.setString(1, engineRef);
            int rowsDeleted = stmt.executeUpdate();

            return rowsDeleted > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'eliminazione del motore", e);
        }
    }

    /* =====================
       FIND
       ===================== */

    @Override
    public Optional<Engine> findById(long id) {
        return findSingle(FIND_BY_ID_SQL, ps -> ps.setLong(1, id));
    }

    @Override
    public Optional<Engine> findByEngineRef(String engineRef) {
        return findSingle(FIND_BY_REF_SQL, ps -> ps.setString(1, engineRef));
    }

    @Override
    public List<Engine> findByEngineCode(String engineCode) {
        return findList(FIND_BY_CODE_SQL, ps -> ps.setString(1, engineCode));
    }

    @Override
    public List<Engine> findByStatus(EngineStatus status) {
        return findList(FIND_BY_STATUS_SQL, ps -> ps.setString(1, status.name()));
    }

    @Override
    public List<Engine> findAll() {
        return findList(FIND_ALL_SQL, null);
    }

    @Override
    public List<Engine> search(String keyword) {
        String k = "%" + keyword + "%";
        return findList(SEARCH_SQL, ps -> {
            for (int i = 1; i <= 5; i++) {
                ps.setString(i, k);
            }
        });
    }

    /* =====================
       KPI
       ===================== */

    @Override
    public int countByStatus(EngineStatus status) {
        return count(COUNT_BY_STATUS_SQL, ps -> ps.setString(1, status.name()));
    }

    @Override
    public int countInWorkshop() {
        return count(COUNT_IN_WORKSHOP_SQL, null);
    }

    @Override
    public int countDeliveredBetween(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from/to non possono essere null");
        }
        return count(COUNT_DELIVERED_BETWEEN_SQL, ps -> {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
        });
    }

    /* =====================
       Utility
       ===================== */

    private Optional<Engine> findSingle(String sql, SQLConsumer<PreparedStatement> consumer) {
        List<Engine> list = findList(sql, consumer);
        return list.stream().findFirst();
    }

    private List<Engine> findList(String sql, SQLConsumer<PreparedStatement> consumer) {
        List<Engine> engines = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (consumer != null) {
                consumer.accept(stmt);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    engines.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore DB", e);
        }

        return engines;
    }

    private int count(String sql, SQLConsumer<PreparedStatement> consumer) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (consumer != null) {
                consumer.accept(stmt);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore nel conteggio", e);
        }
    }

    /* =====================
       Mapping
       ===================== */

    private Engine mapRow(ResultSet rs) throws SQLException {
        Date delivery = rs.getDate("delivery_date");
        LocalDate deliveryDate = (delivery != null) ? delivery.toLocalDate() : null;

        return new Engine(
                rs.getLong("id"),
                rs.getString("engine_ref"),
                rs.getString("engine_code"),
                rs.getLong("customer_id"),
                rs.getDate("intake_date").toLocalDate(),
                EngineStatus.valueOf(rs.getString("status")),
                deliveryDate,
                rs.getString("notes")
        );
    }

    /* =====================
       Functional interface
       ===================== */
    @FunctionalInterface
    private interface SQLConsumer<T> {
        void accept(T t) throws SQLException;
    }

    @Override
    public Optional<Engine> findByCustomerAndEngineCode(String customer, String engineCode) {
        return findSingle(
                FIND_BY_CUSTOMER_AND_ENGINE_CODE_SQL,
                ps -> {
                    ps.setString(1, customer);
                    ps.setString(2, engineCode);
                }
        );
    }



    @Override
    public int getNextSequenceForYear(int year) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(GET_NEXT_SEQUENCE_FOR_YEAR)) {

            ps.setInt(1, year);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // +1 qui = ritorno il prossimo valore
                    return rs.getInt("last_seq") + 1;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Errore nel recupero del progressivo engine_ref per l'anno " + year, e
            );
        }

        // fallback teorico
        return 1;
    }
}
