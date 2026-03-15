package com.grocerypos.ui.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

/**
 * Tiện ích chuyển đổi màn hình.
 */
public class NavigationHelper {
    private static final Logger log = LoggerFactory.getLogger(NavigationHelper.class);
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationHelper.class.getResource("/fxml/" + fxmlPath));
            Scene scene = new Scene(loader.load());
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            log.error("Không thể chuyển màn hình đến: {}", fxmlPath, e);
        }
    }

    public static <T> T openDialog(String fxmlPath, String title, java.util.function.Consumer<T> initializer) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationHelper.class.getResource("/fxml/" + fxmlPath));
            javafx.stage.Stage dialog = new javafx.stage.Stage();
            dialog.setTitle(title);
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialog.initOwner(primaryStage);
            dialog.setResizable(false); // Đặt resizable trước khi nạp cảnh
            
            javafx.scene.Scene scene = new Scene(loader.load());
            dialog.setScene(scene);
            
            T controller = loader.getController();
            if (initializer != null) {
                initializer.accept(controller);
            }
            
            // Không gọi sizeToScene() vì ta đã thiết lập prefWidth cứng trong FXML
            dialog.showAndWait();
            
            return controller;
        } catch (IOException e) {
            log.error("Không thể mở dialog: {}", fxmlPath, e);
            return null;
        }
    }
}
