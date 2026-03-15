package com.grocerypos.ui.controllers;

import com.grocerypos.core.session.SessionManager;
import com.grocerypos.ui.utils.NavigationHelper;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller màn hình lựa chọn Menu chính.
 */
public class MenuController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(MenuController.class);

    @FXML private VBox cardPOS;
    @FXML private VBox cardAdmin;

    @FXML
    private void openPOS() {
        log.info("Chuyển sang màn hình Lịch sử bán hàng (POS)...");
        NavigationHelper.navigateTo("pos/order-history.fxml");
    }

    @FXML
    private void openAdmin() {
        log.info("Chuyển sang màn hình Quản lý (Admin)...");
        NavigationHelper.navigateTo("admin-layout.fxml");
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        NavigationHelper.navigateTo("login-view.fxml");
    }
}
