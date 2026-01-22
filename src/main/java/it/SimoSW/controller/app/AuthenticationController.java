package it.SimoSW.controller.app;

import it.SimoSW.model.User;
import it.SimoSW.model.dao.UserDAO;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public class AuthenticationController {

    private final UserDAO userDAO;

    public AuthenticationController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }


    public Optional<User> login(String username, String password) {

        User user = userDAO.findByUsername(username);

        if (user == null) {
            return Optional.empty();
        }

        String hashedInput = hashPassword(password);

        if (hashedInput.equals(user.getPasswordHash())) {
            return Optional.of(user);
        }

        return Optional.empty();
    }


    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hash algorithm not available", e);
        }
    }


}
