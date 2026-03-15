package com.grocerypos.ui.controllers;

import com.grocerypos.core.util.MoneyUtils;
import com.grocerypos.inventory.entity.StockEntry;
import com.grocerypos.inventory.entity.Supplier;
import com.grocerypos.inventory.service.InventoryService;
import com.grocerypos.product.entity.Product;
import com.grocerypos.product.service.ProductService;
import com.grocerypos.ui.AppContext;
import com.grocerypos.ui.utils.AlertHelper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InventoryController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(InventoryController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private ComboBox<Product> productCombo;
    @FXML private ComboBox<Supplier> supplierCombo;
    @FXML private TextField quantityField;
    @FXML private TextField priceField;
    @FXML private TextField noteField;

    @FXML private TableView<StockEntry> historyTable;
    @FXML private TableColumn<StockEntry, String> colDate;
    @FXML private TableColumn<StockEntry, String> colProductName;
    @FXML private TableColumn<StockEntry, String> colSupplierName;
    @FXML private TableColumn<StockEntry, String> colQty;
    @FXML private TableColumn<StockEntry, String> colCost;
    @FXML private TableColumn<StockEntry, String> colTotal;
    @FXML private TableColumn<StockEntry, String> colNote;

    private final InventoryService inventoryService;
    private final ProductService productService;
    private final ObservableList<StockEntry> historyData = FXCollections.observableArrayList();

    public InventoryController() {
        this.inventoryService = AppContext.get(InventoryService.class);
        this.productService = AppContext.get(ProductService.class);
    }

    @FXML
    public void initialize() {
        setupCombos();
        setupTable();
        loadHistory();
    }

    private void setupCombos() {
        // Tải sản phẩm
        runInBackground(productService::findAll, products -> {
            productCombo.getItems().setAll(products);
        }, e -> {});
        
        productCombo.setConverter(new StringConverter<>() {
            @Override public String toString(Product p) { return p == null ? "" : p.getName() + " [" + p.getBarcode() + "]"; }
            @Override public Product fromString(String s) { return null; }
        });

        // Tải nhà cung cấp
        runInBackground(inventoryService::getAllSuppliers, suppliers -> {
            supplierCombo.getItems().setAll(suppliers);
        }, e -> {});

        supplierCombo.setConverter(new StringConverter<>() {
            @Override public String toString(Supplier s) { return s == null ? "" : s.getName(); }
            @Override public Supplier fromString(String s) { return null; }
        });
    }

    private void setupTable() {
        colDate.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getEntryDate().format(DATE_FORMATTER)));
        colQty.setCellValueFactory(p -> new ReadOnlyStringWrapper(String.format("%,.0f", p.getValue().getQuantity())));
        colCost.setCellValueFactory(p -> new ReadOnlyStringWrapper(MoneyUtils.formatVND(p.getValue().getCostPrice().doubleValue())));
        colTotal.setCellValueFactory(p -> {
            double total = p.getValue().getQuantity().doubleValue() * p.getValue().getCostPrice().doubleValue();
            return new ReadOnlyStringWrapper(MoneyUtils.formatVND(total));
        });
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        
        // ProductName & SupplierName cần join hoặc load bổ sung, tạm thời hiển thị ID
        colProductName.setCellValueFactory(p -> new ReadOnlyStringWrapper("SP ID: " + p.getValue().getProductId()));
        colSupplierName.setCellValueFactory(p -> new ReadOnlyStringWrapper("NCC ID: " + p.getValue().getSupplierId()));

        historyTable.setItems(historyData);
    }

    @FXML
    private void handleEntry() {
        if (!validateInput()) return;

        StockEntry entry = new StockEntry();
        entry.setProductId(productCombo.getValue().getId());
        entry.setSupplierId(supplierCombo.getValue().getId());
        entry.setQuantity(new BigDecimal(quantityField.getText()));
        entry.setCostPrice(new BigDecimal(priceField.getText()));
        entry.setNote(noteField.getText());

        runInBackground(
            () -> {
                inventoryService.addStock(entry);
                return null;
            },
            res -> {
                AlertHelper.showInfo("Thành công", "Đã nhập hàng vào kho.");
                clearForm();
                loadHistory();
            },
            e -> AlertHelper.showError("Lỗi", e.getMessage())
        );
    }

    private boolean validateInput() {
        if (productCombo.getValue() == null || supplierCombo.getValue() == null) {
            AlertHelper.showWarning("Thiếu thông tin", "Vui lòng chọn sản phẩm và nhà cung cấp.");
            return false;
        }
        try {
            Double.parseDouble(quantityField.getText());
            Double.parseDouble(priceField.getText());
        } catch (Exception e) {
            AlertHelper.showWarning("Lỗi", "Số lượng và giá phải là số.");
            return false;
        }
        return true;
    }

    private void clearForm() {
        productCombo.setValue(null);
        quantityField.clear();
        priceField.clear();
        noteField.clear();
    }

    @FXML
    private void loadHistory() {
        runInBackground(
            ((com.grocerypos.inventory.service.impl.InventoryServiceImpl)inventoryService)::getAllHistory,
            history -> historyData.setAll(history),
            e -> log.error("Lỗi tải lịch sử", e)
        );
    }
}
