package com.grocerypos.ui.controllers;

import com.grocerypos.customer.entity.Customer;
import com.grocerypos.customer.service.CustomerService;
import com.grocerypos.ui.AppContext;
import com.grocerypos.ui.utils.AlertHelper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomerFormController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(CustomerFormController.class);

    @FXML private Label titleLabel;
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextArea notesField;
    @FXML private CheckBox activeCheck;

    private final CustomerService customerService;
    private Customer currentCustomer;
    private Runnable onSaveSuccess;

    public CustomerFormController() {
        this.customerService = AppContext.get(CustomerService.class);
    }

    @FXML
    public void initialize() {
        // Form tối giản, ưu đãi mặc định 1% được xử lý khi lưu
    }

    public void setCustomer(Customer customer) {
        this.currentCustomer = customer;
        if (customer != null) {
            titleLabel.setText("Sửa Thông Tin Hội Viên");
            nameField.setText(customer.getName());
            phoneField.setText(customer.getPhone());
            addressField.setText(customer.getAddress());
            notesField.setText(customer.getNotes());
            activeCheck.setSelected(customer.isActive());
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) return;

        Customer customer = currentCustomer != null ? currentCustomer : new Customer();
        customer.setName(nameField.getText().trim());
        customer.setPhone(phoneField.getText().trim());
        customer.setAddress(addressField.getText().trim());
        
        // Luôn đặt mức ưu đãi là 1% (0.01) theo yêu cầu
        customer.setDiscountRate(0.01);
        
        customer.setNotes(notesField.getText().trim());
        customer.setActive(activeCheck.isSelected());

        runInBackground(
            () -> (customer.getId() == null) ? customerService.save(customer) : customerService.update(customer),
            res -> {
                if (onSaveSuccess != null) onSaveSuccess.run();
                closeStage();
            },
            e -> AlertHelper.showError("Lỗi", e.getMessage())
        );
    }

    private boolean validateInput() {
        if (nameField.getText().isBlank()) {
            AlertHelper.showWarning("Thiếu thông tin", "Vui lòng nhập tên khách hàng.");
            return false;
        }
        if (phoneField.getText().isBlank()) {
            AlertHelper.showWarning("Thiếu thông tin", "Vui lòng nhập số điện thoại.");
            return false;
        }
        return true;
    }

    public void setOnSaveSuccess(Runnable callback) { this.onSaveSuccess = callback; }
    @FXML private void handleCancel() { closeStage(); }
    private void closeStage() { ((Stage) nameField.getScene().getWindow()).close(); }
}
