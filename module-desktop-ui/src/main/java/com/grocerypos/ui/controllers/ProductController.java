package com.grocerypos.ui.controllers;

import com.grocerypos.core.util.MoneyUtils;
import com.grocerypos.product.entity.Category;
import com.grocerypos.product.entity.Product;
import com.grocerypos.product.service.CategoryService;
import com.grocerypos.product.service.ProductService;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;

public class ProductController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    @FXML private TextField searchField;
    @FXML private ComboBox<Category> categoryFilter;
    @FXML private TableView<Product> productTable;
    
    // Cập nhật ID khớp với FXML
    @FXML private TableColumn<Product, Long> idCol;
    @FXML private TableColumn<Product, Product> imageCol;
    @FXML private TableColumn<Product, String> nameCol;
    @FXML private TableColumn<Product, String> barcodeCol;
    @FXML private TableColumn<Product, String> categoryCol;
    @FXML private TableColumn<Product, String> sellPriceCol;
    @FXML private TableColumn<Product, Double> stockCol;
    @FXML private TableColumn<Product, String> statusCol;
    @FXML private TableColumn<Product, Product> actionCol;

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ObservableList<Product> productData = FXCollections.observableArrayList();

    public ProductController() {
        this.productService = AppContext.get(ProductService.class);
        this.categoryService = AppContext.get(CategoryService.class);
    }

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        loadData();
    }

    private void setupTable() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        barcodeCol.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        
        sellPriceCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(MoneyUtils.formatVND(param.getValue().getSellPrice())));
        statusCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().isActive() ? "Đang bán" : "Ngừng"));
        
        setupImageColumn();
        setupActionColumn();
        
        productTable.setItems(productData);
    }

    private void setupImageColumn() {
        imageCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        imageCol.setCellFactory(param -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitHeight(40);
                imageView.setFitWidth(40);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                if (empty || product == null || product.getImageUrl() == null) {
                    setGraphic(null);
                } else {
                    try {
                        File file = new File(product.getImageUrl());
                        if (file.exists()) {
                            imageView.setImage(new Image(new FileInputStream(file)));
                            setGraphic(imageView);
                        } else {
                            setGraphic(null);
                        }
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    private void setupActionColumn() {
        actionCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("✎");
            private final Button btnDelete = new Button("🗑");
            private final HBox pane = new HBox(15, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-text-fill: #3498DB; -fx-background-color: #EBF5FB; -fx-cursor: hand; -fx-background-radius: 5;");
                btnDelete.setStyle("-fx-text-fill: #E74C3C; -fx-background-color: #FDEDEC; -fx-cursor: hand; -fx-background-radius: 5;");
                btnEdit.setOnAction(event -> handleEdit(getItem()));
                btnDelete.setOnAction(event -> handleDelete(getItem()));
            }

            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void setupFilters() {
        runInBackground(
            categoryService::findAll,
            categories -> {
                categoryFilter.getItems().clear();
                categoryFilter.getItems().add(null);
                categoryFilter.getItems().addAll(categories);
            },
            e -> log.error("Lỗi tải danh mục", e)
        );

        categoryFilter.setConverter(new StringConverter<>() {
            @Override public String toString(Category c) { return c == null ? "-- Tất cả danh mục --" : c.getName(); }
            @Override public Category fromString(String s) { return null; }
        });
    }

    @FXML
    public void loadData() {
        handleSearch();
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        Category selectedCat = categoryFilter.getValue();
        Long catId = selectedCat != null ? selectedCat.getId() : null;

        runInBackground(
            () -> productService.search(keyword, catId),
            products -> productData.setAll(products),
            e -> AlertHelper.showError("Lỗi", "Không thể tải sản phẩm: " + e.getMessage())
        );
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        categoryFilter.setValue(null);
        loadData();
    }

    @FXML
    private void handleAdd() {
        NavigationHelper.openDialog("product-form.fxml", "Thêm Sản Phẩm", (ProductFormController controller) -> {
            controller.setOnSaveSuccess(this::loadData);
        });
    }

    private void handleEdit(Product product) {
        if (product == null) return;
        NavigationHelper.openDialog("product-form.fxml", "Sửa Sản Phẩm", (ProductFormController controller) -> {
            controller.setProduct(product);
            controller.setOnSaveSuccess(this::loadData);
        });
    }

    private void handleDelete(Product product) {
        if (product == null) return;
        boolean confirm = AlertHelper.showConfirm("Xác nhận", "Bạn có chắc muốn xóa sản phẩm '" + product.getName() + "'?");
        if (confirm) {
            runInBackground(
                () -> { productService.delete(product.getId()); return null; },
                res -> loadData(),
                e -> AlertHelper.showError("Lỗi", e.getMessage())
            );
        }
    }
}
