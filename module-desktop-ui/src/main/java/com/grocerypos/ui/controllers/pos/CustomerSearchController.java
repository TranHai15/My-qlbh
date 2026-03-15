package com.grocerypos.ui.controllers.pos;

import com.grocerypos.core.util.MoneyUtils;
import com.grocerypos.customer.entity.Customer;
import com.grocerypos.customer.service.CustomerService;
import com.grocerypos.ui.AppContext;
import com.grocerypos.ui.controllers.BaseController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class CustomerSearchController extends BaseController {

    @FXML private TextField searchField;
    @FXML private TextField newNameField;
    @FXML private TextField newPhoneField;
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> colName;
    @FXML private TableColumn<Customer, String> colPhone;
    @FXML private TableColumn<Customer, String> colPoints;

    private final CustomerService customerService;
    private final ObservableList<Customer> customerData = FXCollections.observableArrayList();
    private Consumer<Customer> onCustomerSelected;

    public CustomerSearchController() {
        this.customerService = AppContext.get(CustomerService.class);
    }

    @FXML
    public void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        
        // Hiển thị điểm tích lũy
        colPoints.setCellValueFactory(data -> new SimpleStringProperty(String.format("%,.0f", data.getValue().getRewardPoints())));

        customerTable.setItems(customerData);
        
        searchField.textProperty().addListener((obs, oldVal, newVal) -> handleSearch());
        
        customerTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleSelect();
            }
        });
        
        loadAll();
    }

    private void loadAll() {
        runInBackground(customerService::findAll, customerData::setAll, e -> {});
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        runInBackground(() -> customerService.search(keyword), customerData::setAll, e -> {});
    }

    @FXML
    private void handleQuickAdd() {
        String name = newNameField.getText().trim();
        String phone = newPhoneField.getText().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            return;
        }

        Customer customer = Customer.builder()
                .name(name)
                .phone(phone)
                .rewardPoints(0.0)
                .active(true)
                .build();

        runInBackground(
            () -> customerService.save(customer),
            newCustomer -> {
                customerData.add(0, newCustomer);
                customerTable.getSelectionModel().select(newCustomer);
                newNameField.clear();
                newPhoneField.clear();
            },
            e -> {}
        );
    }

    @FXML
    private void handleSelect() {
        Customer selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected != null && onCustomerSelected != null) {
            onCustomerSelected.accept(selected);
            closeStage();
        }
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    private void closeStage() {
        ((Stage) searchField.getScene().getWindow()).close();
    }

    public void setOnCustomerSelected(Consumer<Customer> callback) {
        this.onCustomerSelected = callback;
    }
}
