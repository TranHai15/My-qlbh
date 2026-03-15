package com.grocerypos.ui.controllers.pos;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
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
import com.grocerypos.ui.utils.NavigationHelper;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Optional;

public class POSController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(POSController.class);

    @FXML private TextField barcodeField;
    @FXML private TextField searchField;
    @FXML private Label customerLabel;
    @FXML private Label pointsLabel;
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
    @FXML private Label expectedPointsLabel;
    @FXML private Label availablePointsLabel;
    @FXML private TextField usePointsField;
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
    private double promoDiscount = 0;
    private double pointsToUse = 0;

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
            }
            @Override
            protected void updateItem(CartItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setGraphic(null);
                else { txtQty.setText(String.format("%.1f", item.getQuantity())); setGraphic(container); }
            }
        });

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
        if (newQty <= 0) return;
        if (newQty > item.getProduct().getStockQuantity()) {
            AlertHelper.showWarning("Hết hàng", "Sản phẩm chỉ còn " + item.getProduct().getStockQuantity());
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

        usePointsField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                if (newVal.isEmpty()) pointsToUse = 0;
                else {
                    pointsToUse = Double.parseDouble(newVal);
                    if (currentCustomer != null && pointsToUse > currentCustomer.getRewardPoints()) {
                        pointsToUse = currentCustomer.getRewardPoints();
                        usePointsField.setText(String.valueOf(pointsToUse));
                    }
                }
                updateSummary();
            } catch (Exception e) { pointsToUse = 0; }
        });

        amountPaidField.textProperty().addListener((obs, oldVal, newVal) -> calculateChange());

        barcodeField.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.F1) barcodeField.requestFocus();
                    else if (event.getCode() == KeyCode.F2) searchField.requestFocus();
                    else if (event.getCode() == KeyCode.F3) onSelectCustomer();
                    else if (event.getCode() == KeyCode.F12) onCheckout();
                    else if (event.getCode() == KeyCode.ESCAPE) onBackToHistory();
                });
            }
        });
    }

    private void addProductByBarcode(String barcode) {
        runInBackground(
            () -> productService.findByBarcode(barcode).orElseThrow(() -> new AppException("Mã không tồn tại: " + barcode)),
            this::addProduct,
            e -> AlertHelper.showError("Lỗi", e.getMessage())
        );
    }

    private void searchAndAddProduct(String keyword) {
        runInBackground(
            () -> productService.search(keyword, null),
            products -> { if (!products.isEmpty()) addProduct(products.get(0)); },
            e -> {}
        );
    }

    private void addProduct(Product product) {
        if (product.getStockQuantity() <= 0) { AlertHelper.showError("Hết hàng", "Sản phẩm đã hết!"); return; }
        Optional<CartItem> existing = cart.getItems().stream().filter(i -> i.getProduct().getId().equals(product.getId())).findFirst();
        if (existing.isPresent()) updateQty(existing.get(), 1);
        else { CartItem item = new CartItem(product, 1); cart.addItem(item); cartItems.add(item); }
        updateSummary();
    }

    private void updateSummary() {
        double subtotal = cart.getSubtotal();
        DiscountResult dr = discountEngine.calculate(cart, currentCustomer);
        promoDiscount = dr.getDiscountAmount();
        
        if (currentCustomer == null) {
            pointsToUse = 0;
            usePointsField.clear();
        }

        // Quy đổi điểm -> Tiền giảm giá: 1 điểm = 1,000đ
        double pointMoney = pointsToUse * 1000;
        double total = Math.max(0, subtotal - promoDiscount - pointMoney);

        subtotalLabel.setText(MoneyUtils.formatVND(subtotal));
        discountLabel.setText(MoneyUtils.formatVND(promoDiscount + pointMoney));
        totalAmountLabel.setText(MoneyUtils.formatVND(total));
        
        // Dự kiến điểm nhận được: 1% tổng trả / 1000
        double earnedPoints = Math.floor((total * 0.01) / 1000);
        expectedPointsLabel.setText(String.format("+ Tích thêm: %,.0f điểm", earnedPoints));
        
        if (currentCustomer != null) {
            availablePointsLabel.setText(String.format("(Có: %,.0f)", currentCustomer.getRewardPoints()));
            pointsLabel.setText(String.format("Điểm: %,.0f", currentCustomer.getRewardPoints()));
        } else {
            availablePointsLabel.setText("(Có: 0)");
            pointsLabel.setText("Điểm: 0");
        }
        calculateChange();
    }

    private void calculateChange() {
        try {
            double pointMoney = pointsToUse * 1000;
            double total = Math.max(0, cart.getSubtotal() - promoDiscount - pointMoney);
            String input = amountPaidField.getText().trim().replace(",", "");
            double amountPaid = input.isEmpty() ? 0 : Double.parseDouble(input);
            changeAmountLabel.setText(MoneyUtils.formatVND(Math.max(0, amountPaid - total)));
        } catch (Exception e) { changeAmountLabel.setText("0 đ"); }
    }

    @FXML
    private void onSelectCustomer() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/pos/customer-search.fxml"));
            javafx.scene.Parent root = loader.load();
            CustomerSearchController controller = loader.getController();
            controller.setOnCustomerSelected(customer -> {
                this.currentCustomer = customer;
                this.pointsToUse = 0; // Reset điểm dùng khi đổi khách mới
                this.usePointsField.clear();
                this.customerLabel.setText(customer.getName() + " (" + customer.getPhone() + ")");
                updateSummary();
                barcodeField.requestFocus();
            });
            Stage stage = new Stage();
            stage.setTitle("Quản lý khách hàng");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            
            // Đặt kích thước Scene khớp với prefWidth/Height trong FXML (900x650)
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 900, 650);
            stage.setScene(scene);
            
            // Chống việc cửa sổ bị co lại quá nhỏ
            stage.setMinWidth(800);
            stage.setMinHeight(500);
            
            stage.show();
        } catch (Exception e) { 
            e.printStackTrace(); // Hiện lỗi chi tiết ra console
            AlertHelper.showError("Lỗi", "Không thể mở cửa sổ tìm khách hàng: " + e.getMessage()); 
        }
    }

    @FXML
    private void onScanQR() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh QR/Mã vạch");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
        File file = fileChooser.showOpenDialog(barcodeField.getScene().getWindow());
        if (file != null) {
            try {
                BufferedImage bufferedImage = ImageIO.read(file);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(bufferedImage)));
                Result result = new MultiFormatReader().decode(bitmap);
                addProductByBarcode(result.getText());
            } catch (Exception e) { AlertHelper.showError("QR Error", "Không thể đọc mã từ ảnh này."); }
        }
    }

    @FXML
    private void handleUseAllPoints() {
        if (currentCustomer != null) {
            // Số điểm tối đa cần dùng (Tổng tiền / 1000)
            double maxPointsNeeded = Math.floor((cart.getSubtotal() - promoDiscount) / 1000);
            double pointsToApply = Math.min(currentCustomer.getRewardPoints(), maxPointsNeeded);
            usePointsField.setText(String.format("%.0f", pointsToApply));
        }
    }

    @FXML
    private void onCheckout() {
        if (cart.getItems().isEmpty()) { AlertHelper.showWarning("Lỗi", "Giỏ hàng trống!"); return; }
        double pointMoney = pointsToUse * 1000;
        double total = Math.max(0, cart.getSubtotal() - promoDiscount - pointMoney);
        String input = amountPaidField.getText().trim().replace(",", "");
        double amountPaid = input.isEmpty() ? 0 : Double.parseDouble(input);

        if (amountPaid < total && currentCustomer == null) {
            AlertHelper.showError("Lỗi", "Cần chọn khách hàng để thanh toán.");
            return;
        }

        runInBackground(
            () -> {
                // Sử dụng phương thức checkout mới để chạy tất cả trong 1 Transaction duy nhất
                return orderService.checkout(
                    cart, 
                    currentCustomer != null ? currentCustomer.getId() : null, 
                    promoDiscount, 
                    pointsToUse, 
                    amountPaid
                );
            },
            order -> {
                AlertHelper.showInfo("Thành công", "Đã lưu hóa đơn: " + order.getOrderCode());
                onCancel();
            },
            e -> {
                log.error("Lỗi giao dịch POS", e);
                AlertHelper.showError("Lỗi giao dịch", "Không thể hoàn tất thanh toán. Vui lòng kiểm tra lại kết nối.");
            }
        );
    }

    @FXML
    private void onCancel() {
        cart = new Cart();
        cartItems.clear();
        currentCustomer = null;
        pointsToUse = 0; // Reset điểm về 0
        customerLabel.setText("Khách vãng lai");
        usePointsField.clear();
        amountPaidField.clear();
        updateSummary();
        barcodeField.requestFocus();
    }

    @FXML private void onBackToHistory() { NavigationHelper.navigateTo("pos/order-history.fxml"); }
    @FXML private void onPrintPreview() { AlertHelper.showInfo("In ấn", "Đang in hóa đơn tạm tính..."); }
}
