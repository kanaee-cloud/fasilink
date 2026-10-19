package id.ac.campusfacility.controller;

import id.ac.campusfacility.model.User;
import id.ac.campusfacility.service.AuthService;
import id.ac.campusfacility.util.ViewNavigator;
import id.ac.campusfacility.validation.AuthorizationException;
import id.ac.campusfacility.validation.ValidationException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private HBox errorBox;
    @FXML private Label errorMessageLabel;
    @FXML private Button loginButton;
    @FXML private ProgressIndicator progressIndicator;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        hideError();
    }

    @FXML
    public void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        hideError();

        if (username == null || username.trim().isBlank()) {
            showError("Silakan masukkan username Anda.");
            usernameField.requestFocus();
            return;
        }

        if (password == null || password.trim().isBlank()) {
            showError("Silakan masukkan password Anda.");
            passwordField.requestFocus();
            return;
        }

        setLoading(true);

        try {
            User user = authService.login(username, password);
            ViewNavigator.navigateTo("/fxml/AppShell.fxml", "Dashboard Utama");
        } catch (ValidationException | AuthorizationException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Terjadi kesalahan sistem saat proses login: " + e.getMessage());
        } finally {
            setLoading(false);
        }
    }

    private void showError(String message) {
        errorMessageLabel.setText(message);
        errorBox.setVisible(true);
        errorBox.setManaged(true);
    }

    private void hideError() {
        errorBox.setVisible(false);
        errorBox.setManaged(false);
        errorMessageLabel.setText("");
    }

    private void setLoading(boolean loading) {
        loginButton.setDisable(loading);
        progressIndicator.setVisible(loading);
        progressIndicator.setManaged(loading);
    }
}
