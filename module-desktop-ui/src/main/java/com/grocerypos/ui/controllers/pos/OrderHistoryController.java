package com.grocerypos.ui.controllers.pos;

import com.grocerypos.core.util.MoneyUtils;
import com.grocerypos.customer.service.CustomerService;
import com.grocerypos.order.entity.Order;
import com.grocerypos.order.service.OrderService;
import com.grocerypos.ui.AppContext;
import com.grocerypos.ui.controllers.BaseController;
import com.grocerypos.ui.utils.NavigationHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.List;

public class OrderHistoryController extends BaseController {

    @FXML private TextField searchField;
    @FXML private DatePicker datePicker;
    @FXML private TableView<Order> orderTable;
    @FXML private TableColumn<Order, String> colCode;
    @FXML private TableColumn<Order, String> colDate;
    @FXML private TableColumn<Order, String> colCustomer;
    @FXML private TableColumn<Order, String> colTotal;
    @FXML private TableColumn<Order, String> colStatus;
    @FXML private TableColumn<Order, Void> colActions;

    private final OrderService orderService = AppContext.get(OrderService.class);
    private final CustomerService customerService = AppContext.get(CustomerService.class);

    @FXML
    public void initialize() {
        setupTable();
        loadData();
    }

    private void setupTable() {
        colCode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrderCode()));
        colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCreatedAt().toString()));
        colTotal.setCellValueFactory(data -> new SimpleStringProperty(MoneyUtils.formatVND(data.getValue().getTotalAmount())));
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().toString()));
        
        colCustomer.setCellValueFactory(data -> {
            Long customerId = data.getValue().getCustomerId();
            if (customerId == null) return new SimpleStringProperty("Khách lẻ");
            return new SimpleStringProperty(
                customerService.findById(customerId).map(c -> c.getName()).orElse("?")
            );
        });

        // Nút xem chi tiết/in lại (nếu cần)
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnView = new Button("Chi tiết");
            {
                btnView.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    // showOrderDetails(order);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(new HBox(10, btnView));
            }
        });
    }

    @FXML
    public void loadData() {
        runInBackground(
            () -> orderService.findAll(),
            orders -> orderTable.setItems(FXCollections.observableList(orders)),
            e -> {}
        );
    }

    @FXML
    private void goToPOS() {
        NavigationHelper.navigateTo("pos/pos-view.fxml");
    }
}
