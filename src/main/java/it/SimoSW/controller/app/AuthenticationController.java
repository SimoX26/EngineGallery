package it.SimoSW.controller.app;

import it.SimoSW.model.User;
import it.SimoSW.model.dao.UserDAO;

import java.util.Optional;

public class AuthenticationController {

    private final UserDAO userDAO;

    public AuthenticationController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Autentica un utente dato username e password.
     *
     * @param username username dell'utente
     * @param password password in chiaro
     * @return utente autenticato, se le credenziali sono valide
     */
    public Optional<User> login(String username, String password) {

        User user = userDAO.findByUsername(username);

        if (user == null) {
            return Optional.empty();
        }

        // Controllo password (versione base, da migliorare se usi hash)
        if (user.getPasswordHash().equals(password)) {
            return Optional.of(user);
        }

        return Optional.empty();
    }
}
