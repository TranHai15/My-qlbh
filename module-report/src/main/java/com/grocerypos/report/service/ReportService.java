package com.grocerypos.report.service;

import com.grocerypos.report.dto.RevenueReportDTO;
import com.grocerypos.report.dto.TopProductDTO;
import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    // Doanh thu & lợi nhuận theo ngày
    List<RevenueReportDTO> getRevenueReport(LocalDate from, LocalDate to);

    // Top sản phẩm bán chạy
    List<TopProductDTO> getTopSellingProducts(LocalDate from, LocalDate to, int limit);

    // Báo cáo tồn kho thấp
    List<com.grocerypos.product.entity.Product> getLowStockReport(double threshold);
}
