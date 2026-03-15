package com.grocerypos.ui.controllers;

import com.grocerypos.core.util.BarcodeUtils;
import com.grocerypos.core.util.ImageUtils;
import com.grocerypos.product.entity.Category;
import com.grocerypos.product.entity.Product;
import com.grocerypos.product.service.CategoryService;
import com.grocerypos.product.service.ProductService;
import com.grocerypos.ui.AppContext;
import com.grocerypos.ui.utils.AlertHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ProductFormController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(ProductFormController.class);

    @FXML private Label titleLabel;
    @FXML private TextField nameField;
    @FXML private TextField barcodeField;
    @FXML private ComboBox<Category> categoryCombo;
    @FXML private TextField costPriceField;
    @FXML private TextField sellPriceField;
    @FXML private TextArea descriptionArea;
    @FXML private CheckBox activeCheck;
    @FXML private ImageView productImageView;
    @FXML private ImageView qrImageView;

    private final ProductService productService;
    private final CategoryService categoryService;
    private Product currentProduct;
    private File selectedImageFile;
    private Runnable onSaveSuccess;

    public ProductFormController() {
        this.productService = AppContext.get(ProductService.class);
        this.categoryService = AppContext.get(CategoryService.class);
    }

    @FXML
    public void initialize() {
        setupCategoryCombo();
        setupNumberFields();
        
        // Tự động cập nhật QR Preview khi nhập mã vạch
        barcodeField.textProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && !newV.isBlank()) {
                updateQRPreview(newV);
            } else {
                qrImageView.setImage(null);
            }
        });
    }

    private void setupCategoryCombo() {
        runInBackground(
            categoryService::findAll,
            categories -> {
                categoryCombo.getItems().setAll(categories);
                if (currentProduct != null && currentProduct.getCategoryId() != null) {
                    categories.stream()
                            .filter(c -> c.getId().equals(currentProduct.getCategoryId()))
                            .findFirst()
                            .ifPresent(c -> categoryCombo.setValue(c));
                }
            },
            e -> log.error("Lỗi tải danh mục", e)
        );

        categoryCombo.setConverter(new StringConverter<>() {
            @Override public String toString(Category c) { return c == null ? "" : c.getName(); }
            @Override public Category fromString(String s) { return null; }
        });
    }

    private void setupNumberFields() {
        // Chỉ cho phép nhập số (Chỉ còn giá bán)
        addNumberFilter(sellPriceField);
    }

    private void addNumberFilter(TextField field) {
        field.textProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && !newV.matches("\\d*(\\.\\d*)?")) {
                field.setText(oldV);
            }
        });
    }

    public void setProduct(Product product) {
        this.currentProduct = product;
        if (product != null) {
            titleLabel.setText("Chỉnh Sửa Sản Phẩm");
            nameField.setText(product.getName());
            barcodeField.setText(product.getBarcode());
            
            // Gán giá trị số (Dùng Platform.runLater để đảm bảo UI nạp xong)
            Platform.runLater(() -> {
                sellPriceField.setText(formatNumber(product.getSellPrice()));
            });

            descriptionArea.setText(product.getDescription());
            activeCheck.setSelected(product.isActive());
            
            if (product.getImageUrl() != null) {
                loadImage(product.getImageUrl(), productImageView);
            }
            updateQRPreview(product.getBarcode());
        }
    }

    private String formatNumber(double value) {
        if (value == (long) value) return String.format("%d", (long) value);
        else return String.format("%s", value);
    }

    @FXML
    private void handleGenerateBarcode() {
        barcodeField.setText(BarcodeUtils.generateRandomBarcode());
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh sản phẩm");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Ảnh (PNG, JPG)", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(nameField.getScene().getWindow());
        if (file != null) {
            this.selectedImageFile = file;
            loadImage(file.getAbsolutePath(), productImageView);
        }
    }

    private void updateQRPreview(String content) {
        runInBackground(
            () -> BarcodeUtils.generateQRCode(content, "preview_tmp"),
            path -> {
                if (path != null) loadImage(path, qrImageView);
            },
            e -> log.error("Lỗi tạo QR preview", e)
        );
    }

    private void loadImage(String path, ImageView imageView) {
        try {
            File file = new File(path);
            if (file.exists()) {
                imageView.setImage(new Image(new FileInputStream(file)));
            }
        } catch (FileNotFoundException e) {
            log.warn("Không tìm thấy ảnh: {}", path);
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) return;

        // Quan trọng: Sử dụng object hiện tại nếu là sửa
        Product product = currentProduct != null ? currentProduct : new Product();
        product.setName(nameField.getText().trim());
        product.setBarcode(barcodeField.getText().trim());
        product.setDescription(descriptionArea.getText());
        product.setCategoryId(categoryCombo.getValue() != null ? categoryCombo.getValue().getId() : null);
        
        try {
            product.setSellPrice(Double.parseDouble(sellPriceField.getText().isEmpty() ? "0" : sellPriceField.getText()));
        } catch (NumberFormatException e) {
            AlertHelper.showWarning("Lỗi định dạng", "Giá tiền không hợp lệ.");
            return;
        }
        
        product.setActive(activeCheck.isSelected());

        runInBackground(
            () -> {
                // 1. Lưu ảnh chính thức
                if (selectedImageFile != null) {
                    product.setImageUrl(ImageUtils.saveProductImage(selectedImageFile, product.getBarcode()));
                }
                // 2. Tạo QR chính thức
                BarcodeUtils.generateQRCode(product.getBarcode(), product.getBarcode());
                
                // 3. Lưu hoặc Cập nhật
                if (product.getId() == null) {
                    log.info("Thực hiện lưu sản phẩm mới: {}", product.getName());
                    return productService.save(product);
                } else {
                    log.info("Thực hiện cập nhật sản phẩm (ID: {}): {}", product.getId(), product.getName());
                    return productService.update(product);
                }
            },
            res -> {
                if (onSaveSuccess != null) onSaveSuccess.run();
                closeStage();
            },
            e -> {
                log.error("Lỗi khi lưu sản phẩm", e);
                AlertHelper.showError("Lỗi hệ thống", e.getMessage());
            }
        );
    }

    private boolean validateInput() {
        if (nameField.getText().isBlank()) {
            AlertHelper.showWarning("Thiếu thông tin", "Vui lòng nhập tên sản phẩm.");
            return false;
        }
        if (barcodeField.getText().isBlank()) {
            AlertHelper.showWarning("Thiếu thông tin", "Vui lòng nhập mã vạch.");
            return false;
        }
        return true;
    }

    public void setOnSaveSuccess(Runnable callback) { this.onSaveSuccess = callback; }
    @FXML private void handleCancel() { closeStage(); }
    private void closeStage() { ((Stage) nameField.getScene().getWindow()).close(); }
}
