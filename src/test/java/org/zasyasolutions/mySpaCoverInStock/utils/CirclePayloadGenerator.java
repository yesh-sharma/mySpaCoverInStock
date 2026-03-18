package org.zasyasolutions.mySpaCoverInStock.utils;

import java.util.ArrayList;
import java.util.List;

public class CirclePayloadGenerator {

    private static final int DEFAULT_ANGLE = 360;
    private static final int[] RADIUS_OFFSETS = { 2, 1, 0, -1 };

    public static List<List<String>> generateCirclePayloads() {
        String csvPath = System.getProperty("user.dir") + "/src/test/resources/testdata/spa_cover_circle_radii.csv";
        List<Integer> radii = CsvRadiusReader.readRadii(csvPath);

        List<List<String>> payloads = new ArrayList<>();

        for (int radius : radii) {
            List<String> skuPayload = generateSkuPayload(radius, DEFAULT_ANGLE);
            System.out.println("Circle payload for radius " + radius + " = " + skuPayload);
            payloads.add(skuPayload);
        }

        return payloads;
    }

    public static List<String> generateSkuPayload(int radius, int angle) {
        List<String> skus = new ArrayList<>();

        String originalSku = DimensionConverter.replaceFirstDigit(String.valueOf(radius)) + "-" + angle;
        skus.add(originalSku);

        for (int offset : RADIUS_OFFSETS) {
            if (offset == 0) {
                continue;
            }

            int newRadius = radius + offset;
            if (newRadius <= 0) {
                continue;
            }

            String skuRadius = DimensionConverter.replaceFirstDigit(String.valueOf(newRadius));
            String sku = skuRadius + "-" + angle;
            if (!sku.equals(originalSku)) {
                skus.add(sku);
            }
        }

        return skus;
    }
}
