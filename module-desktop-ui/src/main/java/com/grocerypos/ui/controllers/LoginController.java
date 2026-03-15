package com.grocerypos.ui.controllers;

import com.grocerypos.core.service.AuthService;
import com.grocerypos.ui.AppContext;
import com.grocerypos.ui.utils.NavigationHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller xử lý màn hình Đăng nhập.
 */
public class LoginController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label statusLabel;

    private final AuthService authService;

    public LoginController() {
        this.authService = AppContext.get(AuthService.class);
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        loginButton.setDisable(true);
        statusLabel.setText("Đang xác thực...");

        runInBackground(
            () -> authService.login(username, password),
            session -> {
                log.info("Đăng nhập thành công: {}", session.getDisplayName());
                // Sau khi đăng nhập thành công, chuyển đến Menu lựa chọn
                NavigationHelper.navigateTo("menu-view.fxml");
            },
            e -> {
                log.error("Lỗi đăng nhập: {}", e.getMessage());
                statusLabel.setText("Lỗi: " + e.getMessage());
                loginButton.setDisable(false);
            }
        );
    }
}
