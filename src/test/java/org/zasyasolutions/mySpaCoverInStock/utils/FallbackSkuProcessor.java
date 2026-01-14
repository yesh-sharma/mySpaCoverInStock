package org.zasyasolutions.mySpaCoverInStock.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zasyasolutions.mySpaCoverInStock.model.SpaCoverDimension;

public class FallbackSkuProcessor {

	 public static List<List<String>> generatePayloads(String csvPath) {

	        List<List<String>> payloads = new ArrayList<>();

	        // Step 1: Read CSV
	        List<SpaCoverDimension> dimensions =
	                CsvDimensionReader.readDimensions(csvPath);

	        // Step 2: Generate payload PER entry
	        for (SpaCoverDimension original : dimensions) {

	            List<SpaCoverDimension> fallbacks =
	                    FallbackDimensionGenerator.generateFallbacks(original);

	            List<String> skuPayload =
	                    FallbackDimensionGenerator.generateSKUArray(fallbacks);

	            System.out.println("Payload for " + original + ": " + skuPayload);

	            payloads.add(skuPayload); 
	        }

	        return payloads;
	    }
    
//    public static void main(String[] args) {
//    	String csvPath = "src/test/resources/testdata/spa_cover_dimensions.csv";
//    	 List<List<String>> payload = generatePayloads(csvPath); 
//    	}
    
    
    public static void main(String[] args) {
		String csvPath = "src/test/resources/testdata/spa_cover_dimensions.csv"; 
		
		
		// Print all SKUs
		 List<List<String>> skuPayloads = generatePayloads(csvPath);

		    // Iterate per CSV entry (one API request per entry)
		    for (int i = 0; i < skuPayloads.size(); i++) {

		        List<String> skuList = skuPayloads.get(i);

		        // Build payload for THIS entry
		        Map<String, Object> payload = new HashMap<>();
		        payload.put("skus", skuList);

		        System.out.println("Payload for entry " + (i + 1) + ": " + payload);
		    }
	}
    
}
