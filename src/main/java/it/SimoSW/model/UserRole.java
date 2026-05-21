package it.SimoSW.model;

public enum UserRole {
    ADMIN,
    OPERATOR,
    INSPECTOR;

    public boolean canAccessStatistics() {
        return this == ADMIN || this == INSPECTOR;
    }
}
