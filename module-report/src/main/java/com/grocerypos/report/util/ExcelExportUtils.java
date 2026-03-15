package com.grocerypos.report.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

public class ExcelExportUtils {
    private static final Logger log = LoggerFactory.getLogger(ExcelExportUtils.class);

    /**
     * Xuất dữ liệu ra file Excel.
     *
     * @param filePath    Đường dẫn file lưu
     * @param sheetName   Tên sheet
     * @param headers     Danh sách tiêu đề cột
     * @param data        Danh sách dữ liệu
     * @param rowMapper   Hàm chuyển đổi đối tượng dữ liệu thành mảng Object cho từng hàng
     * @param <T>         Kiểu dữ liệu
     */
    public static <T> void exportToExcel(String filePath, String sheetName, String[] headers, List<T> data, Function<T, Object[]> rowMapper) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            // Create Header Row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create Data Rows
            int rowNum = 1;
            for (T item : data) {
                Row row = sheet.createRow(rowNum++);
                Object[] values = rowMapper.apply(item);
                for (int i = 0; i < values.length; i++) {
                    Cell cell = row.createCell(i);
                    if (values[i] == null) {
                        cell.setCellValue("");
                    } else if (values[i] instanceof Number) {
                        cell.setCellValue(((Number) values[i]).doubleValue());
                    } else {
                        cell.setCellValue(values[i].toString());
                    }
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
            log.info("Đã xuất báo cáo Excel thành công tại: {}", filePath);
        }
    }
}
