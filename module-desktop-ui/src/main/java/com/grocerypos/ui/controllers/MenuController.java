package com.grocerypos.ui.controllers;

import com.grocerypos.core.session.SessionManager;
import com.grocerypos.ui.utils.AlertHelper;
import com.grocerypos.ui.utils.NavigationHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller cho màn hình Menu lựa chọn chức năng.
 */
public class MenuController extends BaseController {

    @FXML private Label welcomeLabel;

    @FXML
    public void initialize() {
        SessionManager.getInstance().getCurrentSession().ifPresent(session -> {
            welcomeLabel.setText("Xin chào, " + session.getDisplayName());
        });
    }

    @FXML
    private void goToPOS() {
        // Sau này sẽ tạo pos-view.fxml
        AlertHelper.showInfo("Thông báo", "Đang chuyển đến màn hình Bán hàng (POS)...");
    }

    @FXML
    private void goToManagement() {
        // Sau này sẽ tạo dashboard-view.fxml
        AlertHelper.showInfo("Thông báo", "Đang chuyển đến màn hình Quản lý...");
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        NavigationHelper.navigateTo("login-view.fxml");
    }
}
