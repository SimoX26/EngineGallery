package it.SimoSW.controller.app;

import it.SimoSW.model.Customer;
import it.SimoSW.model.dao.CustomerDAO;
import it.SimoSW.model.dao.EngineDAO;
import it.SimoSW.model.dao.ImageDAO;
import it.SimoSW.util.generator.EngineRefGenerator;

import java.util.List;

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
}
