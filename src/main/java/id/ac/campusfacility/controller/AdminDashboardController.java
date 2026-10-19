package id.ac.campusfacility.controller;

import id.ac.campusfacility.dto.DashboardSummary;
import id.ac.campusfacility.service.DashboardService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AdminDashboardController {

    @FXML private Label activeRoomsLabel;
    @FXML private Label totalRoomsLabel;
    @FXML private Label availableStockLabel;
    @FXML private Label totalStockLabel;
    @FXML private Label totalSchedulesLabel;
    @FXML private Label pendingBookingsLabel;
    @FXML private Label pendingLoansLabel;
    @FXML private Label totalItemsLabel;

    private final DashboardService dashboardService = new DashboardService();

    @FXML
    public void initialize() {
        loadStatistics();
    }

    @FXML
    public void loadStatistics() {
        Task<DashboardSummary> task = new Task<>() {
            @Override
            protected DashboardSummary call() {
                return dashboardService.getDashboardSummary();
            }
        };

        task.setOnSucceeded(e -> {
            DashboardSummary summary = task.getValue();
            activeRoomsLabel.setText(String.valueOf(summary.getActiveRooms()));
            totalRoomsLabel.setText("/ " + summary.getTotalRooms() + " Total");
            availableStockLabel.setText(String.valueOf(summary.getAvailableQuantityItems()));
            totalStockLabel.setText("/ " + summary.getTotalQuantityItems() + " Total Unit");
            totalSchedulesLabel.setText(String.valueOf(summary.getTotalSchedules()));
            pendingBookingsLabel.setText(String.valueOf(summary.getPendingRoomBookings()));
            pendingLoansLabel.setText(String.valueOf(summary.getPendingInventoryLoans()));
            totalItemsLabel.setText(String.valueOf(summary.getTotalInventoryItems()));
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            activeRoomsLabel.setText("Err");
            availableStockLabel.setText("Err");
            totalSchedulesLabel.setText("Err");
            if (ex != null) ex.printStackTrace();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void handleManageRooms() {
        AppShellController.getInstance().handleNavRooms();
    }

    @FXML
    public void handleManageInventory() {
        AppShellController.getInstance().handleNavInventory();
    }

    @FXML
    public void handleViewSchedule() {
        AppShellController.getInstance().handleNavSchedule();
    }
}
