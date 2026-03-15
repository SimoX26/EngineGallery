package it.SimoSW.controller.app;

import it.SimoSW.model.Customer;
import it.SimoSW.model.dao.CustomerDAO;

import java.util.List;
import java.util.Optional;

public class CustomerController {
    private final CustomerDAO customerDAO;

    public CustomerController(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    public Long findCustomerIdByName(String name) {
        return customerDAO.findIdByName(name);
    }

    public List<Customer> getAllCustomers() {
        return customerDAO.findAll();
    }

    public Long findOrCreateCustomerId(String name) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome cliente non valido");
        }

        String normalized = name.trim();

        Long existingId = customerDAO.findIdByName(normalized);

        if (existingId != null) {
            return existingId;
        }

        Customer newCustomer = new Customer(normalized);
        return customerDAO.save(newCustomer);
    }

    public String findNameById(Long id) {

        if (id == null) {
            return null;
        }

        return customerDAO.findById(id).map(Customer::getName).orElse(null);
    }

    public Optional<Customer> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return customerDAO.findById(id);
    }

    public void updateCustomer(Long id,
                               String name,
                               String companyName,
                               String phone,
                               String email,
                               String notes) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID cliente non valido");
        }

        String normalizedName = normalizeRequired(name, "Nome cliente obbligatorio");
        Customer existing = customerDAO.findById(id)
                .orElseThrow(() -> new IllegalStateException("Cliente non trovato"));

        Customer updated = new Customer(
                id,
                normalizedName,
                normalizeOptional(companyName),
                normalizeOptional(phone),
                normalizeOptional(email),
                normalizeOptional(notes)
        );

        customerDAO.update(updated);
    }

    public boolean hasAssociatedEngines(Long customerId) {
        if (customerId == null || customerId <= 0) {
            return false;
        }
        return customerDAO.countEnginesByCustomerId(customerId) > 0;
    }

    public void deleteCustomer(Long customerId) {
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("ID cliente non valido");
        }

        if (customerDAO.findById(customerId).isEmpty()) {
            throw new IllegalStateException("Cliente non trovato");
        }

        if (hasAssociatedEngines(customerId)) {
            throw new IllegalStateException("Non puoi eliminare un cliente associato a motori esistenti");
        }

        customerDAO.delete(customerId);
    }

    private static String normalizeRequired(String value, String errorMessage) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new IllegalArgumentException(errorMessage);
        }
        return normalized;
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
