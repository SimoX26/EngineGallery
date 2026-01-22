package it.SimoSW.model.dao.database;

import it.SimoSW.model.Engine;
import it.SimoSW.model.EngineStatus;
import it.SimoSW.model.dao.EngineDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseEngineDAO implements EngineDAO {

    /* =====================
       SQL
       ===================== */

    private static final String INSERT_SQL =
            "INSERT INTO engine (engine_code, status) VALUES (?, ?)";

    private static final String UPDATE_SQL =
            "UPDATE engine SET engine_code = ?, status = ? WHERE id = ?";

    private static final String FIND_BY_ID_SQL =
            "SELECT * FROM engine WHERE id = ?";

    private static final String FIND_BY_CODE_SQL =
            "SELECT * FROM engine WHERE engine_code = ?";

    private static final String FIND_ALL_SQL =
            "SELECT * FROM engine";

    private static final String FIND_BY_STATUS_SQL =
            "SELECT * FROM engine WHERE status = ?";

    private static final String FIND_BY_KEYWORD_SQL =
            """
            SELECT DISTINCT e.*
            FROM engine e
            JOIN image i ON e.id = i.engine_id
            WHERE i.keywords LIKE ?
            """;

    /* =====================
       CRUD
       ===================== */

    @Override
    public Engine save(Engine engine) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, engine.getEngineCode());
            stmt.setString(2, engine.getStatus().name());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return new Engine(
                            rs.getLong(1),
                            engine.getEngineCode(),
                            engine.getStatus()
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio del motore", e);
        }

        throw new RuntimeException("Errore durante il salvataggio del motore");
    }

    @Override
    public Engine update(Engine engine) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            stmt.setString(1, engine.getEngineCode());
            stmt.setString(2, engine.getStatus().name());
            stmt.setLong(3, engine.getId());

            stmt.executeUpdate();
            return engine;

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiornamento del motore", e);
        }
    }

    /* =====================
       FIND
       ===================== */

    @Override
    public Optional<Engine> findById(long id) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID_SQL)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca del motore", e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Engine> findByEngineCode(String engineCode) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_CODE_SQL)) {

            stmt.setString(1, engineCode);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca del motore per codice", e);
        }

        return Optional.empty();
    }

    @Override
    public List<Engine> findByStatus(EngineStatus status) {
        List<Engine> engines = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_STATUS_SQL)) {

            stmt.setString(1, status.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    engines.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca dei motori per stato", e);
        }

        return engines;
    }

    @Override
    public List<Engine> findByKeyword(String keyword) {
        List<Engine> engines = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_KEYWORD_SQL)) {

            stmt.setString(1, "%" + keyword + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    engines.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca dei motori per keyword", e);
        }

        return engines;
    }

    @Override
    public List<Engine> findAll() {
        List<Engine> engines = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                engines.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero dei motori", e);
        }

        return engines;
    }

    @Override
    public int countWorkInProgressEngines(){

        String sql = """
                SELECT COUNT(*)\s
                FROM engines\s
                WHERE status = ?
           \s""";

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "IN_PROGRESS");

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore nel conteggio dei motori in lavorazione", e);
        }

        return 0;
    }


    @Override
    public int countMotoriInOfficina(){
        return 0;
    }


    @Override
    public int countMotoriConsegnatiUltimi7Giorni(){
        return 0;
    }


    /* =====================
       Mapping
       ===================== */

    private Engine mapRow(ResultSet rs) throws SQLException {
        return new Engine(rs.getLong("id"), rs.getString("engine_code"), EngineStatus.valueOf(rs.getString("status")));
    }
}
