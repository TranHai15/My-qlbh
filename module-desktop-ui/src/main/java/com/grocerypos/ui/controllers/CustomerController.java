package com.grocerypos.ui.controllers;

import com.grocerypos.core.util.MoneyUtils;
import com.grocerypos.customer.entity.Customer;
import com.grocerypos.customer.service.CustomerService;
import com.grocerypos.ui.AppContext;
import com.grocerypos.ui.utils.AlertHelper;
import com.grocerypos.ui.utils.NavigationHelper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomerController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    @FXML private TextField searchField;
    @FXML private Label totalCountLabel;
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, Long> colId;
    @FXML private TableColumn<Customer, String> colName;
    @FXML private TableColumn<Customer, String> colPhone;
    @FXML private TableColumn<Customer, String> colAddress;
    @FXML private TableColumn<Customer, Double> colPoints;
    @FXML private TableColumn<Customer, String> colDiscount;
    @FXML private TableColumn<Customer, Customer> colAction;

    private final CustomerService customerService;
    private final ObservableList<Customer> customerData = FXCollections.observableArrayList();

    public CustomerController() {
        this.customerService = AppContext.get(CustomerService.class);
    }

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        searchField.setOnAction(e -> handleSearch());
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        
        // Hiển thị Điểm tích lũy
        colPoints.setCellValueFactory(new PropertyValueFactory<>("rewardPoints"));
        colPoints.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%,.0f", item));
                    setStyle("-fx-text-fill: #16A085; -fx-font-weight: bold;");
                }
            }
        });

        // Hiển thị Tỉ lệ chiết khấu
        colDiscount.setCellValueFactory(param -> {
            double rate = param.getValue().getDiscountRate();
            return new ReadOnlyStringWrapper((int)(rate * 100) + "%");
        });

        setupActionColumn();
        customerTable.setItems(customerData);
    }

    private void setupActionColumn() {
        colAction.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("✎");
            private final Button btnDelete = new Button("🗑");
            private final HBox pane = new HBox(15, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-text-fill: #3498DB; -fx-background-color: #EBF5FB; -fx-cursor: hand; -fx-background-radius: 5;");
                btnDelete.setStyle("-fx-text-fill: #E74C3C; -fx-background-color: #FDEDEC; -fx-cursor: hand; -fx-background-radius: 5;");
                btnEdit.setOnAction(event -> handleEditCustomer(getItem()));
                btnDelete.setOnAction(event -> handleDeleteCustomer(getItem()));
            }

            @Override
            protected void updateItem(Customer item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    @FXML
    public void loadData() {
        runInBackground(
            customerService::findAll,
            customers -> {
                customerData.setAll(customers);
                totalCountLabel.setText(customers.size() + " khách hàng");
            },
            e -> log.error("Lỗi tải khách hàng", e)
        );
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        runInBackground(
            () -> customerService.search(keyword),
            customers -> {
                customerData.setAll(customers);
                totalCountLabel.setText(customers.size() + " kết quả");
            },
            e -> AlertHelper.showError("Lỗi", e.getMessage())
        );
    }

    @FXML
    private void handleAddCustomer() {
        NavigationHelper.openDialog("customer-form.fxml", "Thêm Khách Hàng", (CustomerFormController controller) -> {
            controller.setOnSaveSuccess(this::loadData);
        });
    }

    private void handleEditCustomer(Customer customer) {
        if (customer == null) return;
        NavigationHelper.openDialog("customer-form.fxml", "Sửa Khách Hàng", (CustomerFormController controller) -> {
            controller.setCustomer(customer);
            controller.setOnSaveSuccess(this::loadData);
        });
    }

    private void handleDeleteCustomer(Customer customer) {
        if (customer == null) return;
        boolean confirm = AlertHelper.showConfirm("Xác nhận", "Bạn có chắc muốn xóa khách hàng '" + customer.getName() + "'?");
        if (confirm) {
            runInBackground(
                () -> { customerService.delete(customer.getId()); return null; },
                res -> loadData(),
                e -> AlertHelper.showError("Lỗi", e.getMessage())
            );
        }
    }
}
