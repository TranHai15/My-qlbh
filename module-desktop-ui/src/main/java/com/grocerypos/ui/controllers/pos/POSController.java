package com.grocerypos.ui.controllers.pos;

import com.grocerypos.core.exception.AppException;
import com.grocerypos.core.util.MoneyUtils;
import com.grocerypos.customer.entity.Customer;
import com.grocerypos.order.entity.Order;
import com.grocerypos.order.model.Cart;
import com.grocerypos.order.model.CartItem;
import com.grocerypos.order.service.OrderService;
import com.grocerypos.order.service.PaymentService;
import com.grocerypos.product.entity.Product;
import com.grocerypos.product.service.ProductService;
import com.grocerypos.promotion.model.DiscountResult;
import com.grocerypos.promotion.service.DiscountEngine;
import com.grocerypos.ui.AppContext;
import com.grocerypos.ui.controllers.BaseController;
import com.grocerypos.ui.utils.AlertHelper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.Optional;

public class POSController extends BaseController {

    @FXML private TextField barcodeField;
    @FXML private TextField searchField;
    @FXML private Label customerLabel;
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, Number> colIndex;
    @FXML private TableColumn<CartItem, String> colName;
    @FXML private TableColumn<CartItem, CartItem> colQuantity;
    @FXML private TableColumn<CartItem, String> colPrice;
    @FXML private TableColumn<CartItem, String> colDiscount;
    @FXML private TableColumn<CartItem, String> colTotal;
    @FXML private TableColumn<CartItem, CartItem> colActions;
    
    @FXML private Label subtotalLabel;
    @FXML private Label discountLabel;
    @FXML private Label totalAmountLabel;
    @FXML private TextField amountPaidField;
    @FXML private Label changeAmountLabel;
    @FXML private Button btnCheckout;

    private final ProductService productService = AppContext.get(ProductService.class);
    private final OrderService orderService = AppContext.get(OrderService.class);
    private final PaymentService paymentService = AppContext.get(PaymentService.class);
    private final DiscountEngine discountEngine = AppContext.get(DiscountEngine.class);

    private Cart cart = new Cart();
    private ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private Customer currentCustomer = null;
    private double currentDiscount = 0;

    @FXML
    public void initialize() {
        setupTable();
        setupInputHandlers();
        updateSummary();
        barcodeField.requestFocus();
    }

    private void setupTable() {
        cartTable.setItems(cartItems);
        
        colIndex.setCellValueFactory(column-> new ReadOnlyObjectWrapper<>(cartTable.getItems().indexOf(column.getValue()) + 1));
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProduct().getName()));
        colPrice.setCellValueFactory(data -> new SimpleStringProperty(MoneyUtils.formatVND(data.getValue().getUnitPrice())));
        colDiscount.setCellValueFactory(data -> new SimpleStringProperty(MoneyUtils.formatVND(data.getValue().getItemDiscount())));
        colTotal.setCellValueFactory(data -> new SimpleStringProperty(MoneyUtils.formatVND(data.getValue().getLineTotal())));

        // Cột Số lượng: [ - ] [ 1 ] [ + ]
        colQuantity.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        colQuantity.setCellFactory(param -> new TableCell<>() {
            private final Button btnMinus = new Button("-");
            private final TextField txtQty = new TextField();
            private final Button btnPlus = new Button("+");
            private final HBox container = new HBox(5, btnMinus, txtQty, btnPlus);

            {
                container.setAlignment(Pos.CENTER);
                txtQty.setPrefWidth(50);
                txtQty.setAlignment(Pos.CENTER);
                btnMinus.setOnAction(e -> updateQty(getItem(), -1));
                btnPlus.setOnAction(e -> updateQty(getItem(), 1));
                txtQty.setOnAction(e -> {
                    try {
                        double val = Double.parseDouble(txtQty.getText());
                        setQty(getItem(), val);
                    } catch (Exception ex) { txtQty.setText(String.valueOf(getItem().getQuantity())); }
                });
            }

            @Override
            protected void updateItem(CartItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setGraphic(null);
                else {
                    txtQty.setText(String.valueOf(item.getQuantity()));
                    setGraphic(container);
                }
            }
        });

        // Cột Xóa
        colActions.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnDel = new Button("Xóa");
            {
                btnDel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                btnDel.setOnAction(e -> {
                    cartItems.remove(getItem());
                    cart.getItems().remove(getItem());
                    updateSummary();
                });
            }
            @Override
            protected void updateItem(CartItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(btnDel);
            }
        });
    }

    private void updateQty(CartItem item, double delta) {
        if (item == null) return;
        double newQty = item.getQuantity() + delta;
        setQty(item, newQty);
    }

    private void setQty(CartItem item, double newQty) {
        if (newQty <= 0) return;
        // Kiểm tra tồn kho
        if (newQty > item.getProduct().getStockQuantity()) {
            AlertHelper.showWarning("Hết hàng", "Sản phẩm chỉ còn " + item.getProduct().getStockQuantity() + " trong kho.");
            return;
        }
        item.setQuantity(newQty);
        cartTable.refresh();
        updateSummary();
    }

    private void setupInputHandlers() {
        barcodeField.setOnAction(event -> {
            String barcode = barcodeField.getText().trim();
            if (!barcode.isEmpty()) { addProductByBarcode(barcode); barcodeField.clear(); }
        });

        searchField.setOnAction(event -> {
            String keyword = searchField.getText().trim();
            if (!keyword.isEmpty()) { searchAndAddProduct(keyword); searchField.clear(); }
        });

        amountPaidField.textProperty().addListener((obs, oldVal, newVal) -> calculateChange());

        barcodeField.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.F1) barcodeField.requestFocus();
                    else if (event.getCode() == KeyCode.F2) searchField.requestFocus();
                    else if (event.getCode() == KeyCode.F3) onSelectCustomer();
                    else if (event.getCode() == KeyCode.F12) onCheckout();
                    else if (event.getCode() == KeyCode.DELETE) removeItem();
                });
            }
        });
    }

    private void searchAndAddProduct(String keyword) {
        runInBackground(
            () -> productService.search(keyword, null),
            products -> {
                if (products.isEmpty()) AlertHelper.showWarning("Tìm kiếm", "Không tìm thấy sản phẩm: " + keyword);
                else addProduct(products.get(0));
            },
            e -> AlertHelper.showError("Lỗi", e.getMessage())
        );
    }

    private void addProductByBarcode(String barcode) {
        runInBackground(
            () -> productService.findByBarcode(barcode).orElseThrow(() -> new AppException("Không tìm thấy mã vạch: " + barcode)),
            this::addProduct,
            e -> AlertHelper.showError("Lỗi", e.getMessage())
        );
    }

    private void addProduct(Product product) {
        if (product.getStockQuantity() <= 0) {
            AlertHelper.showError("Hết hàng", "Sản phẩm đã hết tồn kho!");
            return;
        }
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existing.isPresent()) {
            updateQty(existing.get(), 1);
        } else {
            CartItem item = new CartItem(product, 1);
            cart.addItem(item);
            cartItems.add(item);
        }
        updateSummary();
    }

    private void removeItem() {
        CartItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            cart.getItems().remove(selected);
            cartItems.remove(selected);
            updateSummary();
        }
    }

    private void updateSummary() {
        double subtotal = cart.getSubtotal();
        DiscountResult dr = discountEngine.calculate(cart, currentCustomer);
        currentDiscount = dr.getDiscountAmount();
        double total = Math.max(0, subtotal - currentDiscount);

        subtotalLabel.setText(MoneyUtils.formatVND(subtotal));
        discountLabel.setText(MoneyUtils.formatVND(currentDiscount));
        totalAmountLabel.setText(MoneyUtils.formatVND(total));
        calculateChange();
    }

    private void calculateChange() {
        try {
            double total = Math.max(0, cart.getSubtotal() - currentDiscount);
            String input = amountPaidField.getText().trim().replace(",", "");
            double amountPaid = input.isEmpty() ? 0 : Double.parseDouble(input);
            double change = paymentService.calculateChange(total, amountPaid);
            changeAmountLabel.setText(MoneyUtils.formatVND(change));
        } catch (Exception e) { changeAmountLabel.setText("Lỗi"); }
    }

    @FXML
    private void onSelectCustomer() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/pos/customer-search.fxml"));
            javafx.scene.Parent root = loader.load();
            CustomerSearchController controller = loader.getController();
            controller.setOnCustomerSelected(customer -> {
                this.currentCustomer = customer;
                this.customerLabel.setText(customer.getName() + " (" + customer.getPhone() + ")");
                updateSummary();
                barcodeField.requestFocus();
            });
            Stage stage = new Stage();
            stage.setTitle("Tìm khách hàng");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) { AlertHelper.showError("Lỗi", e.getMessage()); }
    }

    @FXML
    private void onCheckout() {
        if (cart.getItems().isEmpty()) { AlertHelper.showWarning("Lỗi", "Giỏ hàng trống!"); return; }
        
        double total = Math.max(0, cart.getSubtotal() - currentDiscount);
        String input = amountPaidField.getText().trim().replace(",", "");
        double amountPaid = input.isEmpty() ? 0 : Double.parseDouble(input);

        if (amountPaid < total && currentCustomer == null) {
            AlertHelper.showError("Lỗi", "Cần chọn khách quen để ghi nợ!");
            return;
        }

        runInBackground(
            () -> {
                // Toàn bộ logic lưu đơn + thanh toán + ghi nợ chạy trong 1 Transaction tại đây
                Order order = orderService.createOrder(cart, currentCustomer != null ? currentCustomer.getId() : null, currentDiscount);
                paymentService.processPayment(order.getId(), amountPaid, currentCustomer != null ? currentCustomer.getId() : null);
                return order;
            },
            order -> {
                AlertHelper.showInfo("Thành công", "Đã lưu hóa đơn: " + order.getOrderCode());
                onCancel();
            },
            e -> AlertHelper.showError("Lỗi giao dịch", e.getMessage())
        );
    }

    @FXML
    private void onCancel() {
        cart = new Cart();
        cartItems.clear();
        currentCustomer = null;
        customerLabel.setText("Khách vãng lai");
        amountPaidField.clear();
        updateSummary();
        barcodeField.requestFocus();
    }

    @FXML private void onPrintPreview() { AlertHelper.showInfo("In hóa đơn", "Tính năng đang phát triển..."); }
}
