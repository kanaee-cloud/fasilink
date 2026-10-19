package id.ac.campusfacility.controller;

import id.ac.campusfacility.model.User;
import id.ac.campusfacility.model.enums.Role;
import id.ac.campusfacility.session.UserSession;
import id.ac.campusfacility.util.ViewNavigator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class AppShellController {

    private static AppShellController instance;

    @FXML private VBox navigationContainer;
    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavSchedule;
    @FXML private Button btnNavRooms;
    @FXML private Button btnNavInventory;

    @FXML private Label sidebarUserName;
    @FXML private Label sidebarUserRole;
    @FXML private Label headerTitleLabel;
    @FXML private Label headerUserBadge;
    @FXML private StackPane contentArea;

    public static AppShellController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;
        User currentUser = UserSession.getInstance().getCurrentUser();

        if (currentUser == null) {
            ViewNavigator.navigateTo("/fxml/Login.fxml", "Login");
            return;
        }

        // Tampilkan info user
        sidebarUserName.setText(currentUser.getFullName());
        sidebarUserRole.setText(currentUser.getRole() == Role.ADMIN ? "Administrator Kampus" : "Pengguna / Mahasiswa");

        if (currentUser.getRole() == Role.ADMIN) {
            headerUserBadge.setText("ADMIN");
            headerUserBadge.getStyleClass().setAll("badge", "badge-danger");
            loadView("/fxml/AdminDashboard.fxml", "Dashboard Administrator");
        } else {
            headerUserBadge.setText("USER");
            headerUserBadge.getStyleClass().setAll("badge", "badge-info");
            loadView("/fxml/UserDashboard.fxml", "Dashboard Pengguna");
        }
        setActiveNav(btnNavDashboard);
    }

    public void loadView(String fxmlPath, String pageTitle) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
            headerTitleLabel.setText(pageTitle);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Gagal Memuat Halaman", "Terjadi kesalahan saat memuat tampilan " + fxmlPath + ": " + e.getMessage());
        }
    }

    @FXML
    public void handleNavDashboard() {
        setActiveNav(btnNavDashboard);
        if (UserSession.getInstance().isAdmin()) {
            loadView("/fxml/AdminDashboard.fxml", "Dashboard Administrator");
        } else {
            loadView("/fxml/UserDashboard.fxml", "Dashboard Pengguna");
        }
    }

    @FXML
    public void handleNavSchedule() {
        setActiveNav(btnNavSchedule);
        loadView("/fxml/CampusSchedule.fxml", "Jadwal Perkuliahan Kampus");
    }

    @FXML
    public void handleNavRooms() {
        setActiveNav(btnNavRooms);
        loadView("/fxml/RoomManagement.fxml", "Data & Ketersediaan Ruangan");
    }

    @FXML
    public void handleNavInventory() {
        setActiveNav(btnNavInventory);
        loadView("/fxml/InventoryManagement.fxml", "Data & Stok Inventaris Kampus");
    }

    @FXML
    public void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Logout");
        alert.setHeaderText("Keluar dari Sistem");
        alert.setContentText("Apakah Anda yakin ingin keluar dari aplikasi FasiLink?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                UserSession.getInstance().clear();
                ViewNavigator.navigateTo("/fxml/Login.fxml", "Login");
            }
        });
    }

    private void setActiveNav(Button activeButton) {
        btnNavDashboard.getStyleClass().remove("active");
        btnNavSchedule.getStyleClass().remove("active");
        btnNavRooms.getStyleClass().remove("active");
        btnNavInventory.getStyleClass().remove("active");

        if (activeButton != null && !activeButton.getStyleClass().contains("active")) {
            activeButton.getStyleClass().add("active");
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
