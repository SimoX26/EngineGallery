package it.SimoSW.util.security;

import it.SimoSW.model.User;
import it.SimoSW.model.UserRole;

public final class UserAccessPolicy {
    private UserAccessPolicy() { }

    public static boolean canViewWarehouse(User user) {
        return user != null && user.getRole() != null && user.getRole() != UserRole.OPERATOR;
    }

    public static boolean canViewStatistics(User user) {
        return user != null && user.getRole() == UserRole.ADMIN;
    }

    public static boolean isDeniedPath(User user, String path) {
        return user != null && user.getRole() == UserRole.OPERATOR && (matches(path, "/warehouse")
                || matches(path, "/uploads/warehouse")
                || matches(path, "/statistics"));
    }

    private static boolean matches(String path, String base) {
        return path != null && (path.equals(base) || path.startsWith(base + "/"));
    }

}
