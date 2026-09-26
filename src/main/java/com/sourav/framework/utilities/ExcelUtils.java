package com.sourav.framework.utilities;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class ExcelUtils {
    private ExcelUtils() {
    }

    public static List<List<String>> readRowsFromExcel(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("Excel file does not exist: " + filePath);
        }

        List<List<String>> rows = new ArrayList<>();
        try (FileInputStream stream = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(stream)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                List<String> values = new ArrayList<>();
                for (Cell cell : row) {
                    values.add(getCellValueAsString(cell));
                }
                rows.add(values);
            }
        }
        return rows;
    }

    public static void writeRowsToExcel(String filePath, List<List<String>> rows) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream stream = new FileOutputStream(filePath)) {
            Sheet sheet = workbook.createSheet("Data");
            int rowIndex = 0;
            for (List<String> rowValues : rows) {
                Row row = sheet.createRow(rowIndex++);
                int cellIndex = 0;
                for (String value : rowValues) {
                    row.createCell(cellIndex++).setCellValue(value);
                }
            }
            workbook.write(stream);
        }
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> Double.toString(cell.getNumericCellValue());
            case BOOLEAN -> Boolean.toString(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }
}
