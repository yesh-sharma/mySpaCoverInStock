package org.zasyasolutions.mySpaCoverInStock.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CsvRadiusReader {

    public static List<Integer> readRadii(String csvPath) {
        List<Integer> radii = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String line;
            boolean skipHeader = true;

            while ((line = br.readLine()) != null) {

                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }

                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }

                String[] values = trimmed.split(",");
                int radius = Integer.parseInt(values[0].trim());
                radii.add(radius);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error reading radius CSV file", e);
        }

        return radii;
    }
}
