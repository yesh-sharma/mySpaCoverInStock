package org.zasyasolutions.mySpaCoverInStock.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CircleInventoryResponseOrganizer {

    private static final List<String> COLOR_CODES = Arrays.asList(
        "1104", "1244", "1239", "3132", "3221", "3218"
    );

    private static boolean isFirstWrite = true;

    public static void resetFileState() {
        isFirstWrite = true;
    }

    public static void processAndSaveInventory(String responseJson, List<String> payloadSkus, String outputFilePath) {
        try {
            if (payloadSkus.isEmpty()) {
                System.out.println("No payload SKUs to process");
                return;
            }

            String referenceSku = payloadSkus.get(0);
            System.out.println("\n=== Processing Circle Reference SKU: " + referenceSku + " ===");

            CircleDimensionInfo dimInfo = parseCircleDimensions(referenceSku);
            System.out.println("Reference - Radius: " + dimInfo.radius + ", Angle: " + dimInfo.angle);

            JsonObject response = JsonParser.parseString(responseJson).getAsJsonObject();

            List<JsonObject> allItems = new ArrayList<>();
            addItemsFromArray(allItems, response.getAsJsonArray("inventory"));
            addItemsFromArray(allItems, response.getAsJsonArray("inbound"));
            addItemsFromArray(allItems, response.getAsJsonArray("custom"));

            System.out.println("Total items after filtering (inHandQuantity > 0): " + allItems.size());

            if (allItems.isEmpty()) {
                System.out.println("WARN: No items with available quantity found!");
                if (isFirstWrite) {
                    generateEmptyCSV(dimInfo, outputFilePath);
                    isFirstWrite = false;
                }
                return;
            }

            Map<String, String> selectedSkus = selectBestSkusPerColor(allItems, dimInfo);
            appendToCSV(selectedSkus, dimInfo, outputFilePath);

            System.out.println("OK: CSV row added to: " + outputFilePath);

        } catch (Exception e) {
            System.err.println("Error processing circle inventory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void addItemsFromArray(List<JsonObject> items, JsonArray array) {
        if (array == null) {
            return;
        }

        for (JsonElement element : array) {
            JsonObject item = element.getAsJsonObject();
            String inHandQtyStr = item.get("inHandQuantity").getAsString();
            int inHandQty = Integer.parseInt(inHandQtyStr);

            if (inHandQty > 0) {
                items.add(item);
            }
        }
    }

    private static Map<String, String> selectBestSkusPerColor(List<JsonObject> items, CircleDimensionInfo refDim) {
        Map<String, String> selectedSkus = new LinkedHashMap<>();

        System.out.println("\n=== Searching for Exact Match: " + refDim.radius + "-" + refDim.angle + " ===");

        for (String colorCode : COLOR_CODES) {
            for (JsonObject item : items) {
                String fullSku = item.get("sku").getAsString();
                String[] skuParts = fullSku.split("-");

                if (skuParts.length < 4) {
                    continue;
                }

                String itemRadius = skuParts[0];
                String itemAnglePart = skuParts[1];
                String itemColor = skuParts[3];

                if (itemRadius.equals(refDim.radius)
                        && itemAnglePart.startsWith(refDim.angle)
                        && itemColor.equals(colorCode)) {
                    String inHandQtyStr = item.get("inHandQuantity").getAsString();
                    int inHandQty = Integer.parseInt(inHandQtyStr);

                    if (inHandQty > 0) {
                        selectedSkus.put(colorCode, fullSku);
                        System.out.println("  OK: EXACT MATCH found for " + colorCode + ": " + fullSku + " (qty: " + inHandQty + ")");
                        break;
                    }
                }
            }
        }

        System.out.println("\n=== Searching for Fallback Matches (if needed) ===");

        for (String colorCode : COLOR_CODES) {
            if (selectedSkus.containsKey(colorCode)) {
                continue;
            }

            System.out.println("  INFO: No exact match for " + colorCode + ", searching fallbacks...");

            List<JsonObject> colorMatches = new ArrayList<>();
            for (JsonObject item : items) {
                String fullSku = item.get("sku").getAsString();
                String[] skuParts = fullSku.split("-");

                if (skuParts.length >= 4
                        && skuParts[3].equals(colorCode)) {
                    colorMatches.add(item);
                }
            }

            if (colorMatches.isEmpty()) {
                System.out.println("  WARN: No available SKU found for " + colorCode);
                continue;
            }

            List<JsonObject> sortedFallbacks = sortFallbacksByProximity(colorMatches, refDim);
            JsonObject bestFallback = sortedFallbacks.get(0);
            String bestSku = bestFallback.get("sku").getAsString();
            String inHandQtyStr = bestFallback.get("inHandQuantity").getAsString();

            selectedSkus.put(colorCode, bestSku);
            System.out.println("  OK: FALLBACK found for " + colorCode + ": " + bestSku + " (qty: " + inHandQtyStr + ")");
        }

        return selectedSkus;
    }

    private static List<JsonObject> sortFallbacksByProximity(List<JsonObject> items, CircleDimensionInfo refDim) {
        int refRadius = dimensionToNumeric(refDim.radius);
        List<JsonObject> sorted = new ArrayList<>(items);

        sorted.sort((item1, item2) -> {
            int radius1 = dimensionToNumeric(extractRadius(item1));
            int radius2 = dimensionToNumeric(extractRadius(item2));

            boolean isLarger1 = radius1 > refRadius;
            boolean isLarger2 = radius2 > refRadius;

            if (isLarger1 && !isLarger2) return -1;
            if (!isLarger1 && isLarger2) return 1;

            return Integer.compare(Math.abs(radius1 - refRadius), Math.abs(radius2 - refRadius));
        });

        return sorted;
    }

    private static String extractRadius(JsonObject item) {
        String fullSku = item.get("sku").getAsString();
        String[] skuParts = fullSku.split("-");
        if (skuParts.length < 1) {
            return "";
        }
        return skuParts[0];
    }

    private static CircleDimensionInfo parseCircleDimensions(String sku) {
        String[] parts = sku.split("-");
        String radius = parts.length > 0 ? parts[0] : "";
        String angle = parts.length > 1 ? parts[1] : "";
        return new CircleDimensionInfo(radius, angle);
    }

    private static String replaceLettersWithNumbers(String value) {
        return value.replace("X", "6")
                   .replace("S", "7")
                   .replace("E", "8")
                   .replace("N", "9");
    }

    private static int dimensionToNumeric(String dim) {
        String replaced = replaceLettersWithNumbers(dim);
        try {
            return Integer.parseInt(replaced);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static void generateEmptyCSV(CircleDimensionInfo dimInfo, String outputFilePath) {
        try (FileWriter writer = new FileWriter(outputFilePath, false)) {
            writer.write("Radius,Angle");
            for (String colorCode : COLOR_CODES) {
                writer.write("," + colorCode);
            }
            writer.write("\n");
            System.out.println("Empty circle CSV file created with headers only");
        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + e.getMessage());
        }
    }

    private static void appendToCSV(
        Map<String, String> selectedSkus,
        CircleDimensionInfo dimInfo,
        String outputFilePath
    ) {
        try {
            File file = new File(outputFilePath);
            boolean fileExists = file.exists() && !isFirstWrite;

            FileWriter writer = new FileWriter(outputFilePath, fileExists);

            if (!fileExists || isFirstWrite) {
                writer.write("Radius,Angle");
                for (String colorCode : COLOR_CODES) {
                    writer.write("," + colorCode);
                }
                writer.write("\n");
                isFirstWrite = false;
            }

            String radiusConverted = replaceLettersWithNumbers(dimInfo.radius);
            String angleConverted = replaceLettersWithNumbers(dimInfo.angle);
            writer.write(radiusConverted + "," + angleConverted);

            for (String colorCode : COLOR_CODES) {
                String sku = selectedSkus.getOrDefault(colorCode, "");
                if (!sku.isEmpty()) {
                    sku = replaceLettersWithNumbers(sku);
                }
                writer.write("," + sku);
            }

            writer.write("\n");
            writer.close();

            System.out.println("\n=== Circle CSV Row Added ===");
            System.out.println("Reference Radius: " + radiusConverted + " | Angle: " + angleConverted);
            System.out.println("Color variants found: " + selectedSkus.size() + "/" + COLOR_CODES.size());

        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static class CircleDimensionInfo {
        String radius;
        String angle;

        CircleDimensionInfo(String radius, String angle) {
            this.radius = radius;
            this.angle = angle;
        }
    }
}
