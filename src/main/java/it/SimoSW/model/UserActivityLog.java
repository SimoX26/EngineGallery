package it.SimoSW.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class UserActivityLog {
    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.ITALIAN);

    private long id;
    private String username;
    private String userRole;
    private String actionType;
    private String entityType;
    private String entityId;
    private String description;
    private LocalDateTime createdAt;

    public UserActivityLog() {
    }

    public UserActivityLog(String username,
                           String userRole,
                           String actionType,
                           String entityType,
                           String entityId,
                           String description) {
        this.username = username;
        this.userRole = userRole;
        this.actionType = actionType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedAtLabel() {
        return createdAt != null ? DISPLAY_FORMATTER.format(createdAt) : "";
    }

    public String getActionLabel() {
        if ("CREATE".equals(actionType) && "MOTOR".equals(entityType)) {
            return "aggiunta motore";
        }
        if ("UPDATE".equals(actionType) && "MOTOR".equals(entityType)) {
            return "modifica motore";
        }
        if ("DELETE".equals(actionType) && "MOTOR".equals(entityType)) {
            return "eliminazione motore";
        }
        if ("STATUS_CHANGE".equals(actionType) && "MOTOR".equals(entityType)) {
            return "cambio stato motore";
        }
        if ("CREATE".equals(actionType) && "HYDRAULIC_TEST".equals(entityType)) {
            return "aggiunta prova idraulica";
        }
        if ("UPDATE".equals(actionType) && "HYDRAULIC_TEST".equals(entityType)) {
            return "modifica prova idraulica";
        }
        if ("DELETE".equals(actionType) && "HYDRAULIC_TEST".equals(entityType)) {
            return "eliminazione prova idraulica";
        }
        if ("CREATE".equals(actionType) && "CUSTOMER".equals(entityType)) {
            return "aggiunta cliente";
        }
        if ("UPDATE".equals(actionType) && "CUSTOMER".equals(entityType)) {
            return "modifica cliente";
        }
        if ("DELETE".equals(actionType) && "CUSTOMER".equals(entityType)) {
            return "eliminazione cliente";
        }
        return actionType != null ? actionType : "";
    }

    public String getEntityLabel() {
        if ("MOTOR".equals(entityType)) {
            return "Motore";
        }
        if ("HYDRAULIC_TEST".equals(entityType)) {
            return "Prova idraulica";
        }
        if ("CUSTOMER".equals(entityType)) {
            return "Cliente";
        }
        if ("WAREHOUSE_ITEM".equals(entityType)) {
            return "Articolo magazzino";
        }
        return entityType != null ? entityType : "";
    }

    public boolean isEngineCreate() {
        return "CREATE".equals(actionType) && "MOTOR".equals(entityType);
    }
}
