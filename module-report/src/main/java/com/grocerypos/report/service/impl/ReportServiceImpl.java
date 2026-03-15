package com.grocerypos.report.service.impl;

import com.grocerypos.core.base.BaseRepository;
import com.grocerypos.product.entity.Product;
import com.grocerypos.report.dto.RevenueReportDTO;
import com.grocerypos.report.dto.TopProductDTO;
import com.grocerypos.report.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReportServiceImpl extends BaseRepository implements ReportService {
    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

    @Override
    public List<RevenueReportDTO> getRevenueReport(LocalDate from, LocalDate to) {
        String sql = "SELECT " +
                "date(o.created_at) as order_date, " +
                "SUM(o.total_amount) as total_revenue, " +
                "SUM(oi.cost_price * oi.quantity) as total_cost, " +
                "COUNT(DISTINCT o.id) as order_count " +
                "FROM orders o " +
                "JOIN order_items oi ON o.id = oi.order_id " +
                "WHERE o.status = 'COMPLETED' AND date(o.created_at) BETWEEN ? AND ? " +
                "GROUP BY order_date " +
                "ORDER BY order_date DESC";
        
        return queryList(sql, rs -> RevenueReportDTO.builder()
                .date(LocalDate.parse(rs.getString("order_date")))
                .revenue(rs.getDouble("total_revenue"))
                .cost(rs.getDouble("total_cost"))
                .profit(rs.getDouble("total_revenue") - rs.getDouble("total_cost"))
                .orderCount(rs.getInt("order_count"))
                .build(), from.toString(), to.toString());
    }

    @Override
    public List<TopProductDTO> getTopSellingProducts(LocalDate from, LocalDate to, int limit) {
        String sql = "SELECT " +
                "oi.product_id, " +
                "oi.product_name, " +
                "SUM(oi.quantity) as total_qty, " +
                "SUM(oi.line_total) as total_revenue, " +
                "SUM(oi.line_total - (oi.cost_price * oi.quantity)) as total_profit " +
                "FROM order_items oi " +
                "JOIN orders o ON o.id = oi.order_id " +
                "WHERE o.status = 'COMPLETED' AND date(o.created_at) BETWEEN ? AND ? " +
                "GROUP BY oi.product_id, oi.product_name " +
                "ORDER BY total_revenue DESC " +
                "LIMIT ?";

        return queryList(sql, rs -> TopProductDTO.builder()
                .productId(rs.getLong("product_id"))
                .productName(rs.getString("product_name"))
                .quantity(rs.getDouble("total_qty"))
                .revenue(rs.getDouble("total_revenue"))
                .profit(rs.getDouble("total_profit"))
                .build(), from.toString(), to.toString(), limit);
    }

    @Override
    public List<Product> getLowStockReport(double threshold) {
        String sql = "SELECT p.*, c.name as category_name FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.id " +
                "WHERE p.stock_quantity <= ? AND p.is_active = 1 " +
                "ORDER BY p.stock_quantity ASC";
        
        return queryList(sql, this::mapProductRow, threshold);
    }

    private Product mapProductRow(ResultSet rs) throws SQLException {
        Product p = Product.builder()
                .name(rs.getString("name"))
                .barcode(rs.getString("barcode"))
                .categoryId(rs.getObject("category_id") != null ? rs.getLong("category_id") : null)
                .costPrice(rs.getDouble("cost_price"))
                .sellPrice(rs.getDouble("sell_price"))
                .stockQuantity(rs.getDouble("stock_quantity"))
                .imageUrl(rs.getString("image_url"))
                .description(rs.getString("description"))
                .isActive(rs.getInt("is_active") == 1)
                .categoryName(rs.getString("category_name"))
                .build();
        p.setId(rs.getLong("id"));
        return p;
    }
}
