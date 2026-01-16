package org.zasyasolutions.mySpaCoverInStock.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class InventoryResponseOrganizer {
    
    // Color code priority - these will be the column headers
    private static final List<String> COLOR_CODES = Arrays.asList(
        "1104", "1244", "1239", "3132", "3221", "3218"
    );
    
    // Track if this is the first write to the file
    private static boolean isFirstWrite = true;
    
    /**
     * Reset the first write flag (call this at the start of your test)
     */
    public static void resetFileState() {
        isFirstWrite = true;
    }
    
    /**
     * Main method to process and organize inventory response
     */
    public static void processAndSaveInventory(String responseJson, List<String> payloadSkus, String outputFilePath) {
        try {
            // Take first SKU from payload as reference
            if (payloadSkus.isEmpty()) {
                System.out.println("No payload SKUs to process");
                return;
            }
            
            String referenceSku = payloadSkus.get(0);
            System.out.println("\n=== Processing Reference SKU: " + referenceSku + " ===");
            
            // Parse dimensions from reference SKU
            DimensionInfo dimInfo = parseDimensions(referenceSku);
            System.out.println("Reference - DimA: " + dimInfo.dimA + ", DimB: " + dimInfo.dimB + ", DimC: " + dimInfo.dimC);
            
            JsonObject response = JsonParser.parseString(responseJson).getAsJsonObject();
            
            // Collect ALL items from both inventory AND inbound
            List<JsonObject> allItems = new ArrayList<>();
            
            // Add inventory items with inHandQuantity > 0
            JsonArray inventoryArray = response.getAsJsonArray("inventory");
            for (JsonElement element : inventoryArray) {
                JsonObject item = element.getAsJsonObject();
                String inHandQtyStr = item.get("inHandQuantity").getAsString();
                int inHandQty = Integer.parseInt(inHandQtyStr);
                
                if (inHandQty > 0) {
                    allItems.add(item);
                }
            }
            
            // Add inbound items with inHandQuantity > 0
            JsonArray inboundArray = response.getAsJsonArray("inbound");
            if (inboundArray != null) {
                for (JsonElement element : inboundArray) {
                    JsonObject item = element.getAsJsonObject();
                    String inHandQtyStr = item.get("inHandQuantity").getAsString();
                    int inHandQty = Integer.parseInt(inHandQtyStr);
                    
                    if (inHandQty > 0) {
                        allItems.add(item);
                    }
                }
            }
            
            System.out.println("Total items after filtering (inHandQuantity > 0): " + allItems.size());
            
            if (allItems.isEmpty()) {
                System.out.println("⚠ No items with available quantity found!");
                if (isFirstWrite) {
                    generateEmptyCSV(dimInfo, outputFilePath);
                    isFirstWrite = false;
                }
                return;
            }
            
            // Select the best SKU for each color variant
            Map<String, String> selectedSkus = selectBestSkusPerColor(allItems, dimInfo);
            
            // Append to CSV (or create if first time)
            appendToCSV(selectedSkus, dimInfo, outputFilePath);
            
            System.out.println("✓ CSV row added to: " + outputFilePath);
            
        } catch (Exception e) {
            System.err.println("Error processing inventory: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Select the best SKU for each color variant
     * Priority: Exact match with inHandQuantity > 0, then fallbacks
     */
    private static Map<String, String> selectBestSkusPerColor(List<JsonObject> items, DimensionInfo refDim) {
        Map<String, String> selectedSkus = new LinkedHashMap<>();
        
        // Build reference dimension key (e.g., "N0N0-55")
        String exactDimKey = refDim.dimA + refDim.dimB + "-" + refDim.dimC + refDim.dimC;
        
        System.out.println("\n=== Searching for Exact Match: " + exactDimKey + " ===");
        
        // First pass: Look for EXACT matches for each color
        for (String colorCode : COLOR_CODES) {
            for (JsonObject item : items) {
                String fullSku = item.get("sku").getAsString();
                String[] skuParts = fullSku.split("-");
                
                if (skuParts.length < 4) continue;
                
                String itemDimKey = skuParts[0] + "-" + skuParts[1]; // e.g., "N0N0-55"
                String itemColor = skuParts[3]; // e.g., "1104"
                
                // Check if this is exact match for current color
                if (itemDimKey.equals(exactDimKey) && itemColor.equals(colorCode)) {
                    String inHandQtyStr = item.get("inHandQuantity").getAsString();
                    int inHandQty = Integer.parseInt(inHandQtyStr);
                    
                    if (inHandQty > 0) {
                        selectedSkus.put(colorCode, fullSku);
                        System.out.println("  ✓ EXACT MATCH found for " + colorCode + ": " + fullSku + " (qty: " + inHandQty + ")");
                        break; // Found exact match for this color, move to next color
                    }
                }
            }
        }
        
        System.out.println("\n=== Searching for Fallback Matches (if needed) ===");
        
        // Second pass: Look for fallbacks ONLY for colors not found in exact matches
        for (String colorCode : COLOR_CODES) {
            if (selectedSkus.containsKey(colorCode)) {
                continue; // Already found exact match, skip fallback search
            }
            
            System.out.println("  → No exact match for " + colorCode + ", searching fallbacks...");
            
            // Find ALL items with this color code
            List<JsonObject> colorMatches = new ArrayList<>();
            for (JsonObject item : items) {
                String fullSku = item.get("sku").getAsString();
                String[] skuParts = fullSku.split("-");
                
                if (skuParts.length >= 4 && skuParts[3].equals(colorCode)) {
                    String inHandQtyStr = item.get("inHandQuantity").getAsString();
                    int inHandQty = Integer.parseInt(inHandQtyStr);
                    
                    if (inHandQty > 0) {
                        colorMatches.add(item);
                    }
                }
            }
            
            if (colorMatches.isEmpty()) {
                System.out.println("  ✗ No available SKU found for " + colorCode);
                continue;
            }
            
            // Sort matches by proximity to reference dimensions
            List<JsonObject> sortedFallbacks = sortFallbacksByProximity(colorMatches, refDim);
            
            // Pick the first (best) fallback
            JsonObject bestFallback = sortedFallbacks.get(0);
            String bestSku = bestFallback.get("sku").getAsString();
            String inHandQtyStr = bestFallback.get("inHandQuantity").getAsString();
            
            selectedSkus.put(colorCode, bestSku);
            System.out.println("  ✓ FALLBACK found for " + colorCode + ": " + bestSku + " (qty: " + inHandQtyStr + ")");
        }
        
        return selectedSkus;
    }
    
    /**
     * Sort fallback items by proximity to reference dimensions
     * Priority: Larger dimensions first, then closer to reference
     */
    private static List<JsonObject> sortFallbacksByProximity(List<JsonObject> items, DimensionInfo refDim) {
        int refDimA = dimensionToNumeric(refDim.dimA);
        int refDimB = dimensionToNumeric(refDim.dimB);
        int refAvg = (refDimA + refDimB) / 2;
        
        List<JsonObject> sorted = new ArrayList<>(items);
        
        sorted.sort((item1, item2) -> {
            String sku1 = item1.get("sku").getAsString();
            String sku2 = item2.get("sku").getAsString();
            
            String[] parts1 = sku1.split("-");
            String[] parts2 = sku2.split("-");
            
            if (parts1.length < 2 || parts2.length < 2) return 0;
            
            String dims1 = parts1[0];
            String dims2 = parts2[0];
            
            String dimA1 = dims1.substring(0, 2);
            String dimB1 = dims1.substring(2, 4);
            String dimA2 = dims2.substring(0, 2);
            String dimB2 = dims2.substring(2, 4);
            
            int numA1 = dimensionToNumeric(dimA1);
            int numB1 = dimensionToNumeric(dimB1);
            int numA2 = dimensionToNumeric(dimA2);
            int numB2 = dimensionToNumeric(dimB2);
            
            int avgDim1 = (numA1 + numB1) / 2;
            int avgDim2 = (numA2 + numB2) / 2;
            
            // Prefer larger dimensions over smaller ones
            boolean isLarger1 = avgDim1 > refAvg;
            boolean isLarger2 = avgDim2 > refAvg;
            
            if (isLarger1 && !isLarger2) return -1;
            if (!isLarger1 && isLarger2) return 1;
            
            // Within same category (both larger or both smaller), prefer closer to reference
            return Integer.compare(Math.abs(avgDim1 - refAvg), Math.abs(avgDim2 - refAvg));
        });
        
        return sorted;
    }
    
    /**
     * Parse dimensions from payload SKU (e.g., "N0N0-5")
     */
    private static DimensionInfo parseDimensions(String sku) {
        String[] parts = sku.split("-");
        String dims = parts[0]; // N0N0
        String dimC = parts[1]; // 5
        
        String dimA = dims.substring(0, 2); // N0
        String dimB = dims.substring(2, 4); // N0
        
        return new DimensionInfo(dimA, dimB, dimC);
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
    
    /**
     * Convert dimension code to numeric value for comparison
     */
    private static int dimensionToNumeric(String dim) {
        String replaced = replaceLettersWithNumbers(dim);
        try {
            return Integer.parseInt(replaced);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Generate empty CSV with just headers
     */
    private static void generateEmptyCSV(DimensionInfo dimInfo, String outputFilePath) {
        try (FileWriter writer = new FileWriter(outputFilePath, false)) {
            writer.write("DimA,DimB,DimC");
            for (String colorCode : COLOR_CODES) {
                writer.write("," + colorCode);
            }
            writer.write("\n");
            System.out.println("Empty CSV file created with headers only");
        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + e.getMessage());
        }
    }
    
    /**
     * Append row to CSV (or create with headers if first time)
     */
    private static void appendToCSV(
        Map<String, String> selectedSkus,
        DimensionInfo dimInfo,
        String outputFilePath
    ) {
        try {
            File file = new File(outputFilePath);
            boolean fileExists = file.exists() && !isFirstWrite;
            
            // Open file in append mode (if not first write)
            FileWriter writer = new FileWriter(outputFilePath, fileExists);
            
            // Write header only if this is the first write
            if (!fileExists || isFirstWrite) {
                writer.write("DimA,DimB,DimC");
                for (String colorCode : COLOR_CODES) {
                    writer.write("," + colorCode);
                }
                writer.write("\n");
                isFirstWrite = false;
            }
            
            // Convert reference dimensions to numeric
            String dimAConverted = replaceLettersWithNumbers(dimInfo.dimA);
            String dimBConverted = replaceLettersWithNumbers(dimInfo.dimB);
            
            // Write data row with reference dimensions
            writer.write(dimAConverted + "," + dimBConverted + "," + dimInfo.dimC);
            
            // Write SKU for each color code (or empty if not found)
            for (String colorCode : COLOR_CODES) {
                String sku = selectedSkus.getOrDefault(colorCode, "");
                
                // Convert letters in SKU to numbers
                if (!sku.isEmpty()) {
                    sku = replaceLettersWithNumbers(sku);
                }
                
                writer.write("," + sku);
            }
            
            writer.write("\n");
            writer.close();
            
            System.out.println("\n=== CSV Row Added ===");
            System.out.println("Reference Dimensions: " + dimAConverted + " x " + dimBConverted + " x " + dimInfo.dimC);
            System.out.println("Color variants found: " + selectedSkus.size() + "/" + COLOR_CODES.size());
            
        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Helper class
    static class DimensionInfo {
        String dimA;
        String dimB;
        String dimC;
        
        DimensionInfo(String dimA, String dimB, String dimC) {
            this.dimA = dimA;
            this.dimB = dimB;
            this.dimC = dimC;
        }
    }
}