package org.zasyasolutions.mySpaCoverInStock.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zasyasolutions.mySpaCoverInStock.main.ConfigReader;
import org.zasyasolutions.mySpaCoverInStock.model.SpaCoverDimension;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PayloadGenerator {

	

	 public static List<List<String>> generatePayloads() {
		 String csvPath = "src/test/resources/testdata/spa_cover_dimensions.csv"; 
		 
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
  
	public static Object loginCredentials() {
		// TODO Auto-generated method stub
		String loginEmail = ConfigReader.getProperty("login.email");
    	String loginPassword = ConfigReader.getProperty("login.password");
    	
    	  Map<String, Object> credentials = new HashMap<>();
	        credentials.put("email", loginEmail);
	        credentials.put("password", loginPassword);
	      
	        Gson gson = new GsonBuilder().setPrettyPrinting().create();
	        String jsonOutput = gson.toJson(credentials);
	        System.out.println("Log In Creds : " + jsonOutput);
			return jsonOutput;
	}

	
    

    
}
