package com.grocerypos.ui.controllers;

import com.grocerypos.core.util.MoneyUtils;
import com.grocerypos.customer.service.CustomerService;
import com.grocerypos.order.entity.Order;
import com.grocerypos.order.entity.OrderItem;
import com.grocerypos.order.service.OrderService;
import com.grocerypos.ui.AppContext;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrderDetailController extends BaseController {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @FXML private Label orderCodeLabel;
    @FXML private Label orderDateLabel;
    @FXML private Label statusLabel;
    @FXML private Label customerLabel;
    @FXML private Label staffLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label discountLabel;
    @FXML private Label totalLabel;

    @FXML private TableView<OrderItem> itemTable;
    @FXML private TableColumn<OrderItem, String> colItemName;
    @FXML private TableColumn<OrderItem, String> colItemPrice;
    @FXML private TableColumn<OrderItem, Double> colItemQty;
    @FXML private TableColumn<OrderItem, String> colItemTotal;

    private final OrderService orderService = AppContext.get(OrderService.class);
    private final CustomerService customerService = AppContext.get(CustomerService.class);

    @FXML
    public void initialize() {
        colItemName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colItemPrice.setCellValueFactory(p -> new ReadOnlyStringWrapper(MoneyUtils.formatVND(p.getValue().getUnitPrice())));
        colItemQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colItemTotal.setCellValueFactory(p -> new ReadOnlyStringWrapper(MoneyUtils.formatVND(p.getValue().getLineTotal())));
    }

    public void setOrder(Order order) {
        orderCodeLabel.setText("CHI TIẾT ĐƠN HÀNG #" + order.getOrderCode());
        orderDateLabel.setText("Ngày tạo: " + (order.getCreatedAt() != null ? order.getCreatedAt().format(FORMATTER) : "N/A"));
        statusLabel.setText(order.getStatus().toString());
        staffLabel.setText(order.getCreatedBy() != null ? order.getCreatedBy() : "system");

        if (order.getCustomerId() != null) {
            customerService.findById(order.getCustomerId()).ifPresent(c -> customerLabel.setText(c.getName() + " - " + c.getPhone()));
        } else {
            customerLabel.setText("Khách lẻ");
        }

        subtotalLabel.setText(MoneyUtils.formatVND(order.getSubtotal()));
        discountLabel.setText("-" + MoneyUtils.formatVND(order.getDiscountAmount()));
        totalLabel.setText(MoneyUtils.formatVND(order.getTotalAmount()));

        runInBackground(
            () -> {
                List<OrderItem> items = orderService.getOrderItems(order.getId());
                System.out.println("DEBUG: Order ID = " + order.getId() + ", Items found = " + items.size());
                return items;
            },
            items -> {
                itemTable.setItems(FXCollections.observableList(items));
                System.out.println("DEBUG: Table items set, count = " + itemTable.getItems().size());
            },
            e -> {
                System.err.println("DEBUG: Error loading items: " + e.getMessage());
                e.printStackTrace();
            }
        );
    }

    @FXML
    private void handleClose() {
        ((Stage) orderCodeLabel.getScene().getWindow()).close();
    }
}
