package id.ac.campusfacility.dto;

import id.ac.campusfacility.model.enums.ConflictType;

public class ScheduleConflict {
    private final ConflictType type;
    private final String message;
    private final String entityDescription;

    public ScheduleConflict(ConflictType type, String message, String entityDescription) {
        this.type = type;
        this.message = message;
        this.entityDescription = entityDescription;
    }

    public ConflictType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getEntityDescription() {
        return entityDescription;
    }

    @Override
    public String toString() {
        return "[" + type + "] " + message + (entityDescription != null ? " (" + entityDescription + ")" : "");
    }
}
