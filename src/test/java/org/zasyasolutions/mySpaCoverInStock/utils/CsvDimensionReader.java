package org.zasyasolutions.mySpaCoverInStock.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.zasyasolutions.mySpaCoverInStock.model.SpaCoverDimension;

public class CsvDimensionReader {

    public static List<SpaCoverDimension> readDimensions(String csvPath) {
        List<SpaCoverDimension> dimensions = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String line;
            boolean skipHeader = true;

            while ((line = br.readLine()) != null) {

                // Skip header if present
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }

                String[] values = line.split(",");

                int dimA = Integer.parseInt(values[0].trim());
                int dimB = Integer.parseInt(values[1].trim());
                int dimC = Integer.parseInt(values[2].trim());

                dimensions.add(new SpaCoverDimension(dimA, dimB, dimC));
            }

        } catch (Exception e) {
            throw new RuntimeException("Error reading CSV file", e);
        }

        return dimensions;
    }
}
