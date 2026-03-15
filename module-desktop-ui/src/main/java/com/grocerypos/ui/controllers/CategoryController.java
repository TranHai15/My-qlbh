package com.grocerypos.ui.controllers;

import com.grocerypos.product.entity.Category;
import com.grocerypos.product.service.CategoryService;
import com.grocerypos.ui.AppContext;
import com.grocerypos.ui.utils.AlertHelper;
import com.grocerypos.ui.utils.NavigationHelper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CategoryController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);

    @FXML private TreeTableView<Category> categoryTreeTable;
    @FXML private TreeTableColumn<Category, Category> colImage;
    @FXML private TreeTableColumn<Category, String> colName;
    @FXML private TreeTableColumn<Category, String> colDescription;
    @FXML private TreeTableColumn<Category, String> colStatus;
    @FXML private TreeTableColumn<Category, Category> colAction;

    private final CategoryService categoryService;

    public CategoryController() {
        this.categoryService = AppContext.get(CategoryService.class);
    }

    @FXML
    public void initialize() {
        setupTable();
        loadData();
    }

    private void setupTable() {
        setupImageColumn();
        colName.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getName()));
        colDescription.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getDescription()));
        colStatus.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().isActive() ? "Hoạt động" : "Ngừng"));
        
        setupActionColumn();
    }

    private void setupImageColumn() {
        colImage.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getValue()));
        colImage.setCellFactory(param -> new TreeTableCell<>() {
            private final javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();
            {
                imageView.setFitHeight(30);
                imageView.setFitWidth(30);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(Category category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null || category.getImageUrl() == null) {
                    setGraphic(null);
                } else {
                    try {
                        java.io.File file = new java.io.File(category.getImageUrl());
                        if (file.exists()) {
                            imageView.setImage(new javafx.scene.image.Image(new java.io.FileInputStream(file)));
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
        colAction.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getValue()));
        colAction.setCellFactory(param -> new TreeTableCell<>() {
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
            protected void updateItem(Category category, boolean empty) {
                super.updateItem(category, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    @FXML
    public void loadData() {
        runInBackground(
            categoryService::findAll,
            categories -> {
                buildTree(categories);
            },
            e -> log.error("Lỗi tải danh mục", e)
        );
    }

    private void buildTree(List<Category> allCategories) {
        TreeItem<Category> root = new TreeItem<>(new Category());
        
        Map<Long, List<Category>> childrenByParent = allCategories.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(Category::getParentId));
        
        List<Category> rootCategories = allCategories.stream()
                .filter(c -> c.getParentId() == null)
                .collect(Collectors.toList());

        for (Category cat : rootCategories) {
            root.getChildren().add(createTreeItem(cat, childrenByParent));
        }

        categoryTreeTable.setRoot(root);
    }

    private TreeItem<Category> createTreeItem(Category category, Map<Long, List<Category>> childrenByParent) {
        TreeItem<Category> item = new TreeItem<>(category);
        List<Category> children = childrenByParent.get(category.getId());
        if (children != null) {
            for (Category child : children) {
                item.getChildren().add(createTreeItem(child, childrenByParent));
            }
        }
        item.setExpanded(true);
        return item;
    }

    @FXML
    private void handleAdd() {
        NavigationHelper.openDialog("category-form.fxml", "Thêm Danh Mục", (CategoryFormController controller) -> {
            controller.setOnSaveSuccess(this::loadData);
        });
    }

    private void handleEdit(Category category) {
        if (category == null) return;
        NavigationHelper.openDialog("category-form.fxml", "Sửa Danh Mục", (CategoryFormController controller) -> {
            controller.setCategory(category);
            controller.setOnSaveSuccess(this::loadData);
        });
    }

    private void handleDelete(Category category) {
        if (category == null) return;
        boolean confirm = AlertHelper.showConfirm("Xác nhận", "Bạn có chắc muốn xóa danh mục '" + category.getName() + "'? Lưu ý: Các sản phẩm thuộc danh mục này sẽ bị ảnh hưởng.");
        if (confirm) {
            runInBackground(
                () -> { categoryService.delete(category.getId()); return null; },
                res -> loadData(),
                e -> AlertHelper.showError("Lỗi", e.getMessage())
            );
        }
    }
}
