package it.SimoSW.util.security;

import it.SimoSW.model.User;
import it.SimoSW.model.UserRole;

public final class UserAccessPolicy {
    private UserAccessPolicy() { }

    public static boolean canViewWarehouse(User user) {
        return user != null && !isRestrictedUser(user);
    }

    public static boolean canViewStatistics(User user) {
        return user != null && user.getRole() == UserRole.ADMIN && !isRestrictedUser(user);
    }

    public static boolean isDeniedPath(User user, String path) {
        return isRestrictedUser(user) && (matches(path, "/warehouse")
                || matches(path, "/uploads/warehouse")
                || matches(path, "/statistics"));
    }

    private static boolean matches(String path, String base) {
        return path != null && (path.equals(base) || path.startsWith(base + "/"));
    }

    private static boolean isRestrictedUser(User user) {
        if (user == null || user.getUsername() == null) return false;
        String username = user.getUsername().trim();
        return "maurizio".equalsIgnoreCase(username) || "manuel".equalsIgnoreCase(username);
    }
}
