package id.ac.campusfacility.dto;

public class DashboardSummary {
    private final int totalRooms;
    private final int activeRooms;
    private final int totalInventoryItems;
    private final int totalQuantityItems;
    private final int availableQuantityItems;
    private final int totalSchedules;
    private final int pendingRoomBookings;
    private final int pendingInventoryLoans;

    public DashboardSummary(int totalRooms, int activeRooms, int totalInventoryItems,
                            int totalQuantityItems, int availableQuantityItems,
                            int totalSchedules, int pendingRoomBookings, int pendingInventoryLoans) {
        this.totalRooms = totalRooms;
        this.activeRooms = activeRooms;
        this.totalInventoryItems = totalInventoryItems;
        this.totalQuantityItems = totalQuantityItems;
        this.availableQuantityItems = availableQuantityItems;
        this.totalSchedules = totalSchedules;
        this.pendingRoomBookings = pendingRoomBookings;
        this.pendingInventoryLoans = pendingInventoryLoans;
    }

    public int getTotalRooms() {
        return totalRooms;
    }

    public int getActiveRooms() {
        return activeRooms;
    }

    public int getTotalInventoryItems() {
        return totalInventoryItems;
    }

    public int getTotalQuantityItems() {
        return totalQuantityItems;
    }

    public int getAvailableQuantityItems() {
        return availableQuantityItems;
    }

    public int getTotalSchedules() {
        return totalSchedules;
    }

    public int getPendingRoomBookings() {
        return pendingRoomBookings;
    }

    public int getPendingInventoryLoans() {
        return pendingInventoryLoans;
    }
}
