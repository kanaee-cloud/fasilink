package id.ac.campusfacility.model;

import java.time.LocalDateTime;

public class InventoryItem {
    private Long id;
    private String itemCode;
    private String itemName;
    private String description;
    private Integer totalQuantity;
    private String conditionNotes;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Transient field untuk display di tabel UI
    private Integer availableQuantity;

    public InventoryItem() {
    }

    public InventoryItem(Long id, String itemCode, String itemName, String description,
                         Integer totalQuantity, String conditionNotes, boolean isActive,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.description = description;
        this.totalQuantity = totalQuantity;
        this.conditionNotes = conditionNotes;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.availableQuantity = totalQuantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public String getConditionNotes() {
        return conditionNotes;
    }

    public void setConditionNotes(String conditionNotes) {
        this.conditionNotes = conditionNotes;
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

    public Integer getAvailableQuantity() {
        return availableQuantity != null ? availableQuantity : totalQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public String getDisplayName() {
        return itemCode + " - " + itemName + " (Total: " + totalQuantity + ")";
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
