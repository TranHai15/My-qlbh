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

        // Nút xem chi tiết/sửa
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnView = new Button("Chi tiết");
            private final Button btnEdit = new Button("Sửa");
            {
                btnView.getStyleClass().add("btn-info");
                btnView.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                btnView.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    showOrderDetails(order);
                });
                
                btnEdit.getStyleClass().add("btn-warning");
                btnEdit.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                btnEdit.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    editOrderNotes(order);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView().getItems().get(getIndex()) == null) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(5, btnView, btnEdit));
                }
            }
        });
    }

    private void showOrderDetails(Order order) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/order-detail-dialog.fxml"));
            javafx.scene.Parent root = loader.load();
            com.grocerypos.ui.controllers.OrderDetailController controller = loader.getController();
            controller.setOrder(order);
            
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Chi tiết đơn hàng #" + order.getOrderCode());
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            
            // Set fixed size for the scene to avoid the "too small window" issue
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 800, 650);
            stage.setScene(scene);
            
            // Set minimum size
            stage.setMinWidth(750);
            stage.setMinHeight(550);
            
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            com.grocerypos.ui.utils.AlertHelper.showError("Lỗi", "Không thể mở chi tiết đơn hàng: " + e.getMessage());
        }
    }

    private void editOrderNotes(Order order) {
        TextInputDialog dialog = new TextInputDialog(order.getNotes() != null ? order.getNotes() : "");
        dialog.setTitle("Sửa Đơn Hàng");
        dialog.setHeaderText("Cập nhật ghi chú cho đơn hàng: " + order.getOrderCode());
        dialog.setContentText("Ghi chú mới:");

        dialog.showAndWait().ifPresent(note -> {
            runInBackground(
                () -> {
                    orderService.updateOrderNotes(order.getId(), note);
                    return null;
                },
                res -> {
                    order.setNotes(note);
                    com.grocerypos.ui.utils.AlertHelper.showInfo("Thành công", "Đã cập nhật đơn hàng thành công!");
                },
                e -> com.grocerypos.ui.utils.AlertHelper.showError("Lỗi", "Cập nhật thất bại: " + e.getMessage())
            );
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
