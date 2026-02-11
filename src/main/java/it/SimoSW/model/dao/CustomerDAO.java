package it.SimoSW.model.dao;

import it.SimoSW.model.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerDAO {

    List<Customer> findAll();

    Optional<Customer> findById(Long id);

    Long findIdByName(String name);

    Optional<Customer> findByEmail(String email);

    List<Customer> searchByName(String keyword);

    Long save(Customer customer);

    void update(Customer customer);

    void delete(Long id);

    int countClientiConMotoriInOfficina();

}
