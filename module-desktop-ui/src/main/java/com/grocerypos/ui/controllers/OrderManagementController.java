package com.grocerypos.ui.controllers;

import com.grocerypos.core.util.MoneyUtils;
import com.grocerypos.customer.service.CustomerService;
import com.grocerypos.order.entity.Order;
import com.grocerypos.order.service.OrderService;
import com.grocerypos.ui.AppContext;
import com.grocerypos.ui.utils.AlertHelper;
import com.grocerypos.ui.utils.NavigationHelper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrderManagementController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(OrderManagementController.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private TextField searchField;
    @FXML private DatePicker dateFilter;
    @FXML private TableView<Order> orderTable;
    @FXML private TableColumn<Order, String> colTime;
    @FXML private TableColumn<Order, String> colCode;
    @FXML private TableColumn<Order, String> colCustomer;
    @FXML private TableColumn<Order, String> colTotal;
    @FXML private TableColumn<Order, String> colStatus;
    @FXML private TableColumn<Order, Void> colActions;

    private final OrderService orderService;
    private final CustomerService customerService;
    private final ObservableList<Order> orderData = FXCollections.observableArrayList();

    public OrderManagementController() {
        this.orderService = AppContext.get(OrderService.class);
        this.customerService = AppContext.get(CustomerService.class);
    }

    @FXML
    public void initialize() {
        setupTable();
        handleSearch();
    }

    private void setupTable() {
        colTime.setCellValueFactory(p -> new ReadOnlyStringWrapper(
            p.getValue().getCreatedAt() != null ? p.getValue().getCreatedAt().format(DATE_TIME_FORMATTER) : ""
        ));
        colCode.setCellValueFactory(new PropertyValueFactory<>("orderCode"));
        colTotal.setCellValueFactory(p -> new ReadOnlyStringWrapper(MoneyUtils.formatVND(p.getValue().getTotalAmount())));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        colCustomer.setCellValueFactory(data -> {
            Long customerId = data.getValue().getCustomerId();
            if (customerId == null) return new SimpleStringProperty("Khách lẻ");
            return new SimpleStringProperty(
                customerService.findById(customerId).map(c -> c.getName()).orElse("N/A")
            );
        });

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnView = new Button("Chi tiết");
            {
                btnView.getStyleClass().add("btn-info");
                btnView.setOnAction(event -> showOrderDetails(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(new HBox(10, btnView));
            }
        });

        orderTable.setItems(orderData);
    }

    @FXML
    private void handleSearch() {
        runInBackground(
            () -> orderService.searchOrders(searchField.getText().trim(), dateFilter.getValue()),
            res -> orderData.setAll(res),
            e -> log.error("Lỗi tìm kiếm đơn hàng", e)
        );
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        dateFilter.setValue(null);
        handleSearch();
    }

    private void showOrderDetails(Order order) {
        NavigationHelper.openDialog("order-detail-dialog.fxml", "Chi tiết đơn hàng " + order.getOrderCode(), (OrderDetailController controller) -> {
            controller.setOrder(order);
        });
    }
}
