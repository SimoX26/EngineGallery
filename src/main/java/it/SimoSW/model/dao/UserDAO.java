package it.SimoSW.model.dao;

import it.SimoSW.model.User;

public interface UserDAO {

    User findByUsername(String username);

    User findById(long userId);
}
