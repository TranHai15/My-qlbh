package com.grocerypos.ui.controllers;

import com.grocerypos.inventory.entity.Supplier;
import com.grocerypos.inventory.service.InventoryService;
import com.grocerypos.ui.AppContext;
import com.grocerypos.ui.utils.AlertHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SupplierFormController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(SupplierFormController.class);

    @FXML private Label titleLabel;
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField addressField;

    private final InventoryService inventoryService;
    private Supplier currentSupplier;
    private Runnable onSaveSuccess;

    public SupplierFormController() {
        this.inventoryService = AppContext.get(InventoryService.class);
    }

    public void setSupplier(Supplier supplier) {
        this.currentSupplier = supplier;
        if (supplier != null) {
            titleLabel.setText("Sửa Thông Tin Đối Tác");
            nameField.setText(supplier.getName());
            phoneField.setText(supplier.getPhone());
            emailField.setText(supplier.getEmail());
            addressField.setText(supplier.getAddress());
        }
    }

    public void setOnSaveSuccess(Runnable callback) {
        this.onSaveSuccess = callback;
    }

    @FXML
    private void handleSave() {
        if (nameField.getText().isBlank() || phoneField.getText().isBlank()) {
            AlertHelper.showWarning("Thiếu thông tin", "Vui lòng nhập tên và số điện thoại.");
            return;
        }

        Supplier s = currentSupplier != null ? currentSupplier : new Supplier();
        s.setName(nameField.getText().trim());
        s.setPhone(phoneField.getText().trim());
        s.setEmail(emailField.getText().trim());
        s.setAddress(addressField.getText().trim());

        runInBackground(
            () -> {
                if (currentSupplier == null) {
                    inventoryService.addSupplier(s);
                } else {
                    inventoryService.updateSupplier(s);
                }
                return null;
            },
            res -> {
                if (onSaveSuccess != null) onSaveSuccess.run();
                closeStage();
            },
            e -> AlertHelper.showError("Lỗi", e.getMessage())
        );
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    private void closeStage() {
        ((Stage) nameField.getScene().getWindow()).close();
    }
}
