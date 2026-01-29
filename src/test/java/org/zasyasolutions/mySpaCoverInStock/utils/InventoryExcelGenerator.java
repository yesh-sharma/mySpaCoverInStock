package org.zasyasolutions.mySpaCoverInStock.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Excel generator for inventory with color-coded status
 */
public class InventoryExcelGenerator {
    
    // Color code to name mapping
    private static final Map<String, String> COLOR_NAMES = new LinkedHashMap<>();
    static {
        COLOR_NAMES.put("1104", "Oxford Grey");
        COLOR_NAMES.put("1244", "Brazilian Mahogany");
        COLOR_NAMES.put("1239", "Coffee Brown");
        COLOR_NAMES.put("3132", "Coastal Grey");
        COLOR_NAMES.put("3221", "Mahogany");
        COLOR_NAMES.put("3218", "Mayan Brown");
    }
    
    private static final List<String> COLOR_CODES = new ArrayList<>(COLOR_NAMES.keySet());
    
    /**
     * Append row to Excel file with color formatting
     */
    public static void appendToExcel(
        Map<String, InventoryResponseOrganizer.SkuWithSource> selectedSkus,
        InventoryResponseOrganizer.DimensionInfo dimInfo,
        String outputFilePath
    ) {
        try {
            Workbook workbook;
            Sheet sheet;
            int lastRowNum = 0;
            File file = new File(outputFilePath);
            
            // Load existing workbook or create new one
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                workbook = new XSSFWorkbook(fis);
                sheet = workbook.getSheetAt(0);
                lastRowNum = sheet.getLastRowNum();
                fis.close();
            } else {
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet("Inventory");
                createHeaderRow(workbook, sheet);
                lastRowNum = 0;
            }
            
            // Create new data row
            Row dataRow = sheet.createRow(lastRowNum + 1);
            
            // Create cell styles for different statuses
            CellStyle instockStyle = createInstockStyle(workbook);
            CellStyle inboundStyle = createInboundStyle(workbook);
            CellStyle customStyle = createCustomStyle(workbook);
            CellStyle dimensionStyle = createDimensionStyle(workbook);
            
            // Add dimension cells
            Cell dimACell = dataRow.createCell(0);
            dimACell.setCellValue(replaceLettersWithNumbers(dimInfo.dimA));
            dimACell.setCellStyle(dimensionStyle);
            
            Cell dimBCell = dataRow.createCell(1);
            dimBCell.setCellValue(replaceLettersWithNumbers(dimInfo.dimB));
            dimBCell.setCellStyle(dimensionStyle);
            
            Cell dimCCell = dataRow.createCell(2);
            dimCCell.setCellValue(replaceLettersWithNumbers(dimInfo.dimC));
            dimCCell.setCellStyle(dimensionStyle);
            
            // Add SKU cells with color coding
            int colIndex = 3;
            for (String colorCode : COLOR_CODES) {
                Cell cell = dataRow.createCell(colIndex++);
                InventoryResponseOrganizer.SkuWithSource skuWithSource = selectedSkus.get(colorCode);
                
                if (skuWithSource == null) {
                    cell.setCellValue("custom");
                    cell.setCellStyle(customStyle);
                } else {
                    String sku = replaceLettersWithNumbers(skuWithSource.sku);
                    cell.setCellValue(sku);
                    
                    if (skuWithSource.source.equals("inventory")) {
                        cell.setCellStyle(instockStyle);
                    } else {
                        cell.setCellStyle(inboundStyle);
                    }
                }
            }
            
            // Auto-size columns (only on first write)
            if (lastRowNum == 0) {
                for (int i = 0; i < 3 + COLOR_CODES.size(); i++) {
                    sheet.autoSizeColumn(i);
                }
            }
            
            // Save workbook
            FileOutputStream fos = new FileOutputStream(outputFilePath);
            workbook.write(fos);
            fos.close();
            workbook.close();
            
            System.out.println("✓ Excel row added to: " + outputFilePath);
            
        } catch (IOException e) {
            System.err.println("Error writing Excel file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create header row with formatting
     */
    private static void createHeaderRow(Workbook workbook, Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);
        
        // Dimension headers
        Cell dimAHeader = headerRow.createCell(0);
        dimAHeader.setCellValue("DimA");
        dimAHeader.setCellStyle(headerStyle);
        
        Cell dimBHeader = headerRow.createCell(1);
        dimBHeader.setCellValue("DimB");
        dimBHeader.setCellStyle(headerStyle);
        
        Cell dimCHeader = headerRow.createCell(2);
        dimCHeader.setCellValue("DimC");
        dimCHeader.setCellStyle(headerStyle);
        
        // Color variant headers
        int colIndex = 3;
        for (String colorCode : COLOR_CODES) {
            Cell header = headerRow.createCell(colIndex++);
            String colorName = COLOR_NAMES.get(colorCode);
            header.setCellValue(colorCode + " - " + colorName);
            header.setCellStyle(headerStyle);
        }
    }
    
    /**
     * Create header style (bold, centered, light gray background)
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Add borders
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }
    
    /**
     * Create style for dimension cells (neutral)
     */
    private static CellStyle createDimensionStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // Add borders
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }
    
    /**
     * Create style for instock items (green background)
     */
    private static CellStyle createInstockStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);
        
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // Light green background
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Add borders
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }
    
    /**
     * Create style for inbound items (yellow background)
     */
    private static CellStyle createInboundStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);
        
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // Light yellow background
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Add borders
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }
    
    /**
     * Create style for custom items (light red background)
     */
    private static CellStyle createCustomStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.BLACK.getIndex());
        font.setItalic(true);
        style.setFont(font);
        
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // Light coral/red background
        style.setFillForegroundColor(IndexedColors.CORAL.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Add borders
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }
    
    /**
     * Replace letters with numbers (X=6, S=7, E=8, N=9)
     */
    private static String replaceLettersWithNumbers(String value) {
        return value.replace("X", "6")
                   .replace("S", "7")
                   .replace("E", "8")
                   .replace("N", "9");
    }
}