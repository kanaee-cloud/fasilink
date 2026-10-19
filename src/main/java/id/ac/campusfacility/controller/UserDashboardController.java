package id.ac.campusfacility.controller;

import id.ac.campusfacility.dto.DashboardSummary;
import id.ac.campusfacility.service.DashboardService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class UserDashboardController {

    @FXML private Label activeRoomsLabel;
    @FXML private Label availableStockLabel;
    @FXML private Label totalSchedulesLabel;

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
            availableStockLabel.setText(String.valueOf(summary.getAvailableQuantityItems()));
            totalSchedulesLabel.setText(String.valueOf(summary.getTotalSchedules()));
        });

        task.setOnFailed(e -> {
            activeRoomsLabel.setText("-");
            availableStockLabel.setText("-");
            totalSchedulesLabel.setText("-");
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void handleViewSchedule() {
        AppShellController.getInstance().handleNavSchedule();
    }

    @FXML
    public void handleViewRooms() {
        AppShellController.getInstance().handleNavRooms();
    }

    @FXML
    public void handleViewInventory() {
        AppShellController.getInstance().handleNavInventory();
    }
}
