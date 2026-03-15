package com.grocerypos.ui.controllers;

import com.grocerypos.core.util.MoneyUtils;
import com.grocerypos.product.entity.Product;
import com.grocerypos.report.dto.RevenueReportDTO;
import com.grocerypos.report.dto.TopProductDTO;
import com.grocerypos.report.service.ReportService;
import com.grocerypos.report.util.ExcelExportUtils;
import com.grocerypos.ui.AppContext;
import com.grocerypos.ui.utils.AlertHelper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

public class ReportController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;

    // Revenue Table
    @FXML private TableView<RevenueReportDTO> revenueTable;
    @FXML private TableColumn<RevenueReportDTO, String> colRevDate;
    @FXML private TableColumn<RevenueReportDTO, Integer> colRevOrders;
    @FXML private TableColumn<RevenueReportDTO, String> colRevRevenue;
    @FXML private TableColumn<RevenueReportDTO, String> colRevCost;
    @FXML private TableColumn<RevenueReportDTO, String> colRevProfit;

    // Top Product Table
    @FXML private TableView<TopProductDTO> topProductTable;
    @FXML private TableColumn<TopProductDTO, String> colProdName;
    @FXML private TableColumn<TopProductDTO, Double> colProdQty;
    @FXML private TableColumn<TopProductDTO, String> colProdRevenue;
    @FXML private TableColumn<TopProductDTO, String> colProdProfit;

    // Low Stock Table
    @FXML private TableView<Product> lowStockTable;
    @FXML private TableColumn<Product, String> colStockName;
    @FXML private TableColumn<Product, String> colStockBarcode;
    @FXML private TableColumn<Product, Double> colStockQty;

    private final ReportService reportService;
    private final ObservableList<RevenueReportDTO> revenueData = FXCollections.observableArrayList();
    private final ObservableList<TopProductDTO> topProductData = FXCollections.observableArrayList();
    private final ObservableList<Product> lowStockData = FXCollections.observableArrayList();

    public ReportController() {
        this.reportService = AppContext.get(ReportService.class);
    }

    @FXML
    public void initialize() {
        fromDate.setValue(LocalDate.now().minusDays(30));
        toDate.setValue(LocalDate.now());

        setupTables();
        handleSearch();
    }

    private void setupTables() {
        // Revenue
        colRevDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colRevOrders.setCellValueFactory(new PropertyValueFactory<>("orderCount"));
        colRevRevenue.setCellValueFactory(p -> new ReadOnlyStringWrapper(MoneyUtils.formatVND(p.getValue().getRevenue())));
        colRevCost.setCellValueFactory(p -> new ReadOnlyStringWrapper(MoneyUtils.formatVND(p.getValue().getCost())));
        colRevProfit.setCellValueFactory(p -> new ReadOnlyStringWrapper(MoneyUtils.formatVND(p.getValue().getProfit())));
        revenueTable.setItems(revenueData);

        // Top Product
        colProdName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colProdQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colProdRevenue.setCellValueFactory(p -> new ReadOnlyStringWrapper(MoneyUtils.formatVND(p.getValue().getRevenue())));
        colProdProfit.setCellValueFactory(p -> new ReadOnlyStringWrapper(MoneyUtils.formatVND(p.getValue().getProfit())));
        topProductTable.setItems(topProductData);

        // Low Stock
        colStockName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colStockBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colStockQty.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        lowStockTable.setItems(lowStockData);
    }

    @FXML
    private void handleSearch() {
        LocalDate start = fromDate.getValue();
        LocalDate end = toDate.getValue();

        if (start == null || end == null) {
            AlertHelper.showWarning("Thiếu thông tin", "Vui lòng chọn khoảng thời gian.");
            return;
        }

        runInBackground(() -> reportService.getRevenueReport(start, end), revenueData::setAll, e -> log.error("Lỗi tải BC doanh thu", e));
        runInBackground(() -> reportService.getTopSellingProducts(start, end, 20), topProductData::setAll, e -> log.error("Lỗi tải BC sản phẩm", e));
        runInBackground(() -> reportService.getLowStockReport(10.0), lowStockData::setAll, e -> log.error("Lỗi tải BC tồn kho", e));
    }

    @FXML
    private void exportRevenue() {
        export("Bao_cao_doanh_thu.xlsx", "Doanh Thu", 
            new String[]{"Ngày", "Số Đơn", "Doanh Thu", "Tiền Vốn", "Lợi Nhuận"}, 
            revenueData, 
            item -> new Object[]{item.getDate(), item.getOrderCount(), item.getRevenue(), item.getCost(), item.getProfit()});
    }

    @FXML
    private void exportTopProducts() {
        export("Top_san_pham_ban_chay.xlsx", "Sản Phẩm", 
            new String[]{"Tên Sản Phẩm", "Số Lượng", "Doanh Thu", "Lợi Nhuận"}, 
            topProductData, 
            item -> new Object[]{item.getProductName(), item.getQuantity(), item.getRevenue(), item.getProfit()});
    }

    @FXML
    private void exportLowStock() {
        export("Canh_bao_ton_kho.xlsx", "Tồn Kho", 
            new String[]{"Tên Sản Phẩm", "Mã Vạch", "Số Lượng Tồn"}, 
            lowStockData, 
            item -> new Object[]{item.getName(), item.getBarcode(), item.getStockQuantity()});
    }

    private <T> void export(String defaultFileName, String sheetName, String[] headers, List<T> data, java.util.function.Function<T, Object[]> rowMapper) {
        if (data.isEmpty()) {
            AlertHelper.showWarning("Thông báo", "Không có dữ liệu để xuất.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu file Excel");
        fileChooser.setInitialFileName(defaultFileName);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(revenueTable.getScene().getWindow());

        if (file != null) {
            runInBackground(() -> {
                try {
                    ExcelExportUtils.exportToExcel(file.getAbsolutePath(), sheetName, headers, data, rowMapper);
                } catch (java.io.IOException e) {
                    throw new RuntimeException("Lỗi ghi file Excel: " + e.getMessage(), e);
                }
                return null;
            }, res -> AlertHelper.showInfo("Thành công", "Đã xuất file Excel tại: " + file.getAbsolutePath()), 
            e -> AlertHelper.showError("Lỗi xuất file", e.getMessage()));
        }
    }
}
