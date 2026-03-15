package com.grocerypos.ui.controllers;

import com.grocerypos.core.util.ImageUtils;
import com.grocerypos.product.entity.Category;
import com.grocerypos.product.service.CategoryService;
import com.grocerypos.ui.AppContext;
import com.grocerypos.ui.utils.AlertHelper;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
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

public class CategoryFormController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(CategoryFormController.class);

    @FXML private Label titleLabel;
    @FXML private TextField nameField;
    @FXML private TextArea descField;
    @FXML private ComboBox<Category> parentCombo;
    @FXML private CheckBox activeCheck;
    @FXML private ImageView categoryImageView;

    private final CategoryService categoryService;
    private Category currentCategory;
    private Long pendingParentId;
    private File selectedImageFile;
    private Runnable onSaveSuccess;

    public CategoryFormController() {
        this.categoryService = AppContext.get(CategoryService.class);
    }

    @FXML
    public void initialize() {
        setupParentCombo();
    }

    private void setupParentCombo() {
        parentCombo.getItems().addListener((ListChangeListener<Category>) c -> {
            if (pendingParentId != null) {
                selectParentById(pendingParentId);
            }
        });

        runInBackground(
            categoryService::findAll,
            categories -> {
                parentCombo.getItems().clear();
                parentCombo.getItems().add(null);
                parentCombo.getItems().addAll(categories);
                
                if (currentCategory != null) {
                    parentCombo.getItems().removeIf(cat -> cat != null && cat.getId().equals(currentCategory.getId()));
                }
            },
            e -> log.error("Lỗi tải danh mục", e)
        );

        parentCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Category object) {
                return object == null ? "-- Danh mục gốc --" : object.getName();
            }
            @Override public Category fromString(String string) { return null; }
        });
    }

    public void setCategory(Category category) {
        this.currentCategory = category;
        if (category != null) {
            titleLabel.setText("Chỉnh Sửa Danh Mục");
            nameField.setText(category.getName());
            descField.setText(category.getDescription());
            activeCheck.setSelected(category.isActive());
            
            if (category.getImageUrl() != null) {
                loadImage(category.getImageUrl());
            }

            if (category.getParentId() != null) {
                this.pendingParentId = category.getParentId();
                selectParentById(pendingParentId);
            } else {
                parentCombo.setValue(null);
            }
        }
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn biểu tượng danh mục");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Ảnh (PNG, JPG)", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(nameField.getScene().getWindow());
        if (file != null) {
            this.selectedImageFile = file;
            loadImage(file.getAbsolutePath());
        }
    }

    private void loadImage(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                categoryImageView.setImage(new Image(new FileInputStream(file)));
            }
        } catch (FileNotFoundException e) {
            log.warn("Không tìm thấy ảnh danh mục: {}", path);
        }
    }

    private void selectParentById(Long id) {
        for (Category item : parentCombo.getItems()) {
            if (item != null && item.getId().equals(id)) {
                parentCombo.setValue(item);
                pendingParentId = null;
                break;
            }
        }
    }

    public void setOnSaveSuccess(Runnable callback) {
        this.onSaveSuccess = callback;
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            AlertHelper.showWarning("Thiếu thông tin", "Tên danh mục không được để trống.");
            return;
        }

        Category category = currentCategory != null ? currentCategory : new Category();
        category.setName(name);
        category.setDescription(descField.getText().trim());
        category.setActive(activeCheck.isSelected());
        
        Category parent = parentCombo.getValue();
        category.setParentId(parent != null ? parent.getId() : null);

        runInBackground(
            () -> {
                if (selectedImageFile != null) {
                    category.setImageUrl(ImageUtils.saveProductImage(selectedImageFile, "cat_" + category.getName().hashCode()));
                }
                
                if (category.getId() == null) {
                    return categoryService.save(category);
                } else {
                    return categoryService.update(category);
                }
            },
            result -> {
                if (onSaveSuccess != null) onSaveSuccess.run();
                closeStage();
            },
            e -> AlertHelper.showError("Lỗi hệ thống", e.getMessage())
        );
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    private void closeStage() {
        ((Stage) nameField.getScene().getWindow()).close();
    }
}
