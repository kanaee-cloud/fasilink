package id.ac.campusfacility.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ViewNavigator {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1280, 760);
            scene.getStylesheets().add(ViewNavigator.class.getResource("/css/app.css").toExternalForm());
            primaryStage.setTitle("FasiLink — " + title);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Gagal memuat halaman: " + fxmlPath, e);
        }
    }
}
