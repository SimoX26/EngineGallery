package it.SimoSW.controller.app;

import it.SimoSW.model.User;
import it.SimoSW.model.dao.UserDAO;
import it.SimoSW.util.security.PasswordHashUtil;
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

        if (PasswordHashUtil.verify(password, user.getPasswordHash())) {
            if (PasswordHashUtil.isLegacyHash(user.getPasswordHash())) {
                String upgradedHash = PasswordHashUtil.hash(password);
                userDAO.updatePasswordHash(user.getId(), upgradedHash);
                user.setPasswordHash(upgradedHash);
            }
            return Optional.of(user);
        }

        return Optional.empty();
    }

    public Optional<User> findById(long userId) {
        User user = userDAO.findById(userId);
        return Optional.ofNullable(user);
    }
}
