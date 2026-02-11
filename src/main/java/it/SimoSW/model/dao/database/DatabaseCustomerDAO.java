package it.SimoSW.model.dao.database;

import it.SimoSW.model.Customer;
import it.SimoSW.model.EngineStatus;
import it.SimoSW.model.dao.CustomerDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseCustomerDAO implements CustomerDAO {
    private static final String FIND_ID_BY_NAME_SQL =
            "SELECT id FROM customers WHERE name = ?";

    private static final String COUNT_CLIENTI_IN_OFFICINA_SQL =
            "SELECT COUNT(DISTINCT customer_id) " +
                    "FROM engines " +
                    "WHERE status <> ?";

    private static final String FIND_ALL_SQL =
            "SELECT id, name, company_name, phone, email, notes, created_at " +
                    "FROM customers ORDER BY name";

    private static final String FIND_BY_ID_SQL =
            "SELECT id, name, company_name, phone, email, notes, created_at " +
                    "FROM customers WHERE id = ?";

    private static final String FIND_BY_EMAIL_SQL =
            "SELECT id, name, company_name, phone, email, notes, created_at " +
                    "FROM customers WHERE email = ?";

    private static final String SEARCH_BY_NAME_SQL =
            "SELECT id, name, company_name, phone, email, notes, created_at " +
                    "FROM customers WHERE name LIKE ? ORDER BY name";

    private static final String INSERT_SQL =
            "INSERT INTO customers (name, company_name, phone, email, notes) " +
                    "VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL =
            "UPDATE customers " +
                    "SET name = ?, company_name = ?, phone = ?, email = ?, notes = ? " +
                    "WHERE id = ?";

    private static final String DELETE_SQL =
            "DELETE FROM customers WHERE id = ?";


    @Override
    public List<Customer> findAll() {

        List<Customer> customers = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                customers.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero dei clienti", e);
        }

        return customers;
    }


    @Override
    public Optional<Customer> findById(Long id) {

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID_SQL)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero cliente ID: " + id, e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Customer> findByEmail(String email) {

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_EMAIL_SQL)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore nella ricerca per email: " + email, e);
        }

        return Optional.empty();
    }



    @Override
    public List<Customer> searchByName(String keyword) {

        List<Customer> customers = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SEARCH_BY_NAME_SQL)) {

            stmt.setString(1, "%" + keyword + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore nella ricerca clienti per nome", e);
        }

        return customers;
    }


    @Override
    public Long save(Customer customer) {

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     INSERT_SQL,
                     PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getCompanyName());
            stmt.setString(3, customer.getPhone());
            stmt.setString(4, customer.getEmail());
            stmt.setString(5, customer.getNotes());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio del cliente", e);
        }

        throw new RuntimeException("Impossibile ottenere ID generato per il cliente");
    }


    @Override
    public void update(Customer customer) {

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getCompanyName());
            stmt.setString(3, customer.getPhone());
            stmt.setString(4, customer.getEmail());
            stmt.setString(5, customer.getNotes());
            stmt.setLong(6, customer.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiornamento cliente ID: " + customer.getId(), e);
        }
    }


    @Override
    public void delete(Long id) {

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'eliminazione cliente ID: " + id, e);
        }
    }


    private Customer mapRow(ResultSet rs) throws SQLException {

        return new Customer(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("company_name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("notes"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }

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
