package com.grocerypos.ui.controllers;

import com.grocerypos.core.session.SessionManager;
import com.grocerypos.ui.utils.NavigationHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class AdminLayoutController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(AdminLayoutController.class);

    @FXML private StackPane contentArea;
    @FXML private Label headerTitle;
    @FXML private Label userNameLabel;
    
    @FXML private HBox navDashboard;
    @FXML private HBox navProduct;
    @FXML private HBox navCategory;
    @FXML private HBox navInventory;
    @FXML private HBox navSupplier;
    @FXML private HBox navOrder;
    @FXML private HBox navCustomer;
    @FXML private HBox navReport;

    @FXML
    public void initialize() {
        log.info("Khởi tạo AdminLayoutController...");
        SessionManager.getInstance().getCurrentSession().ifPresent(session -> {
            if (userNameLabel != null) userNameLabel.setText(session.getDisplayName());
        });
        showDashboard();
    }

    @FXML public void showDashboard() { updateUI("Tổng quan hệ thống", navDashboard, "dashboard-placeholder.fxml"); }
    @FXML public void showProducts() { updateUI("Quản lý sản phẩm", navProduct, "product-view.fxml"); }
    @FXML public void showCategories() { updateUI("Quản lý danh mục", navCategory, "category-view.fxml"); }
    @FXML public void showInventory() { updateUI("Quản lý kho hàng", navInventory, "inventory-view.fxml"); }
    @FXML public void showSuppliers() { updateUI("Quản lý nhà cung cấp", navSupplier, "supplier-view.fxml"); }
    @FXML public void showOrders() { updateUI("Quản lý đơn hàng", navOrder, "order-management-view.fxml"); }
    @FXML public void showCustomers() { updateUI("Quản lý khách hàng", navCustomer, "customer-view.fxml"); }
    @FXML public void showReports() { updateUI("Báo cáo & Thống kê", navReport, "report-view.fxml"); }

    private void updateUI(String title, HBox navItem, String fxmlPath) {
        if (headerTitle != null) headerTitle.setText(title);
        setActiveNavItem(navItem);
        loadView(fxmlPath);
    }

    @FXML
    private void backToMenu() { NavigationHelper.navigateTo("menu-view.fxml"); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        NavigationHelper.navigateTo("login-view.fxml");
    }

    private void loadView(String fxmlPath) {
        if (contentArea == null) return;
        try {
            URL resource = getClass().getResource("/fxml/" + fxmlPath);
            if (resource == null) throw new IOException("Resource not found: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(resource);
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            log.error("Lỗi nạp màn hình: {}", fxmlPath, e);
            showDevelopmentPlaceholder(fxmlPath);
        }
    }

    private void showDevelopmentPlaceholder(String fxmlPath) {
        Label label = new Label("Màn hình [" + fxmlPath + "] đang được hoàn thiện... 🌸");
        label.setStyle("-fx-font-size: 18; -fx-text-fill: #BDC3C7; -fx-text-alignment: center;");
        contentArea.getChildren().setAll(label);
    }

    private void setActiveNavItem(HBox activeItem) {
        List<HBox> navItems = Arrays.asList(navDashboard, navProduct, navCategory, navInventory, navSupplier, navOrder, navCustomer, navReport);
        for (HBox item : navItems) {
            if (item != null) item.getStyleClass().remove("nav-item-active");
        }
        if (activeItem != null) activeItem.getStyleClass().add("nav-item-active");
    }
}
