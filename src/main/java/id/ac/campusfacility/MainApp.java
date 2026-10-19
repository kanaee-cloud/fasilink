package id.ac.campusfacility;

import id.ac.campusfacility.config.DatabaseInitializer;
import id.ac.campusfacility.util.ViewNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void init() throws Exception {
        super.init();
        // Inisialisasi basis data SQLite dan seed demo idempotent
        DatabaseInitializer.initialize();
    }

    @Override
    public void start(Stage primaryStage) {
        ViewNavigator.setPrimaryStage(primaryStage);
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(680);
        ViewNavigator.navigateTo("/fxml/Login.fxml", "Login");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
