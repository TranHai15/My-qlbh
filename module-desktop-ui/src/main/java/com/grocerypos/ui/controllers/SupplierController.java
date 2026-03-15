package com.grocerypos.ui.controllers;

import com.grocerypos.inventory.entity.Supplier;
import com.grocerypos.inventory.service.InventoryService;
import com.grocerypos.ui.AppContext;
import com.grocerypos.ui.utils.AlertHelper;
import com.grocerypos.ui.utils.NavigationHelper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SupplierController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(SupplierController.class);

    @FXML private TableView<Supplier> supplierTable;
    @FXML private TableColumn<Supplier, Long> colId;
    @FXML private TableColumn<Supplier, String> colName;
    @FXML private TableColumn<Supplier, String> colPhone;
    @FXML private TableColumn<Supplier, String> colEmail;
    @FXML private TableColumn<Supplier, String> colAddress;
    @FXML private TableColumn<Supplier, Supplier> colAction;

    private final InventoryService inventoryService;
    private final ObservableList<Supplier> supplierData = FXCollections.observableArrayList();

    public SupplierController() {
        this.inventoryService = AppContext.get(InventoryService.class);
    }

    @FXML
    public void initialize() {
        setupTable();
        loadData();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        
        setupActionColumn();
        supplierTable.setItems(supplierData);
    }

    private void setupActionColumn() {
        colAction.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("✎");
            private final HBox pane = new HBox(10, btnEdit);
            {
                btnEdit.setStyle("-fx-text-fill: #3498DB; -fx-background-color: #EBF5FB; -fx-cursor: hand;");
                btnEdit.setOnAction(event -> handleEdit(getItem()));
            }
            @Override
            protected void updateItem(Supplier item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    @FXML
    public void loadData() {
        runInBackground(
            inventoryService::getAllSuppliers,
            suppliers -> supplierData.setAll(suppliers),
            e -> log.error("Lỗi tải NCC", e)
        );
    }

    @FXML
    private void handleAdd() {
        NavigationHelper.openDialog("supplier-form.fxml", "Thêm Nhà Cung Cấp", (SupplierFormController controller) -> {
            controller.setOnSaveSuccess(this::loadData);
        });
    }

    private void handleEdit(Supplier supplier) {
        if (supplier == null) return;
        NavigationHelper.openDialog("supplier-form.fxml", "Sửa Nhà Cung Cấp", (SupplierFormController controller) -> {
            controller.setSupplier(supplier);
            controller.setOnSaveSuccess(this::loadData);
        });
    }
}
