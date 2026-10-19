package id.ac.campusfacility.model;

import java.time.LocalDateTime;

public class Room {
    private Long id;
    private String roomCode;
    private String roomName;
    private String building;
    private Integer floor;
    private Integer capacity;
    private String description;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Room() {
    }

    public Room(Long id, String roomCode, String roomName, String building, Integer floor,
                Integer capacity, String description, boolean isActive,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.roomCode = roomCode;
        this.roomName = roomName;
        this.building = building;
        this.floor = floor;
        this.capacity = capacity;
        this.description = description;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDisplayName() {
        return roomCode + " - " + roomName + " (Kapasitas: " + capacity + ")";
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
