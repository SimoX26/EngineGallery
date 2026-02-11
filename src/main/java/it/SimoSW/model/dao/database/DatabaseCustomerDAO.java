package it.SimoSW.model.dao.database;

import it.SimoSW.model.EngineStatus;
import it.SimoSW.model.dao.CustomerDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseCustomerDAO implements CustomerDAO {
    private static final String FIND_ID_BY_NAME_SQL =
            "SELECT id FROM customers WHERE name = ?";

    private static final String COUNT_CLIENTI_IN_OFFICINA_SQL =
            "SELECT COUNT(DISTINCT customer_id) " +
                    "FROM engines " +
                    "WHERE status <> ?";

    @Override
    public Long findIdByName(String name) {

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ID_BY_NAME_SQL)) {

            stmt.setString(1, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca del cliente per nome: " + name, e);
        }

        // Se non trovato
        throw new RuntimeException("Cliente non trovato: " + name);
    }

    @Override
    public int countClientiConMotoriInOfficina() {

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_CLIENTI_IN_OFFICINA_SQL)) {

            stmt.setString(1, EngineStatus.DELIVERED.name());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Errore nel conteggio clienti con motori in officina",
                    e
            );
        }

        return 0;
    }

}
