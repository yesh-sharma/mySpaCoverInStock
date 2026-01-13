package org.zasyasolutions.mySpaCoverInStock.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zasyasolutions.mySpaCoverInStock.model.SpaCoverDimension;

public class FallbackSkuProcessor {

    public static List<String> processCsv(String csvPath) {

        List<String> allSkus = new ArrayList<>();

        // Step 1: Read CSV
        List<SpaCoverDimension> originalDimensions =
                CsvDimensionReader.readDimensions(csvPath);

        // Step 2: For each row → generate fallbacks → convert to SKU
        for (SpaCoverDimension original : originalDimensions) {

            List<SpaCoverDimension> fallbacks = FallbackDimensionGenerator.generateFallbacks(original);

            List<String> skuList = FallbackDimensionGenerator.generateSKUArray(fallbacks);
            System.out.println("> Payload for "+original+" entry: " + skuList);

            allSkus.addAll(skuList);
        }
        System.out.println("+ Payload for  entry: " + allSkus);
        
        return allSkus;
    }
        
    
    public static void main1(String[] args) {
    	String csvPath = "src/test/resources/testdata/spa_cover_dimensions.csv";
    	List<String> skuArray = processCsv(csvPath); 
		System.out.println(skuArray);
		
    	}
    
    
    public static void main(String[] args) {
		String csvPath = "src/test/resources/testdata/spa_cover_dimensions.csv"; 
		List<String> skuArray = processCsv(csvPath);
		
		// Print all SKUs
		for (String sku : skuArray) {
			System.out.println(sku);
			Map<String, Object> payload = new HashMap<>();
		    payload.put("skus", skuArray);
		    System.out.println("- Payload for "+sku +" entry: " + payload);
		}
	}
    
    
}
