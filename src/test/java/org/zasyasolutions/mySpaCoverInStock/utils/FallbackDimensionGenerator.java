package org.zasyasolutions.mySpaCoverInStock.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.zasyasolutions.mySpaCoverInStock.model.SpaCoverDimension;

public class FallbackDimensionGenerator {

	
	private static final List<String> PRODUCT_SHAPES = Arrays.asList(
	        "Rounded Rectangle Spa Cover"
	        
	    );

	    /**
	     * Generate all fallback dimension combinations
	     */
	    public static List<SpaCoverDimension> generateFallbacks(SpaCoverDimension original) {
	        List<SpaCoverDimension> fallbackAttempts = new ArrayList<>();
	        
	        int altDimA = original.getDimensionA();
	        int altDimB = original.getDimensionB();
	        int altDimC = original.getDimensionC();
	        
	        fallbackAttempts.addAll(generateStandardFallbacksNonProduct(altDimA, altDimB, altDimC));
	    
	        return fallbackAttempts;
	    }

	    /**
	     * Generate standard fallbacks 
	     */
	    private static List<SpaCoverDimension> generateStandardFallbacksNonProduct(
	            int altDimA, int altDimB, int altDimC) {
	        
	        List<SpaCoverDimension> fallbacks = new ArrayList<>();
	        
	        // Original dimensions
	        fallbacks.add(new SpaCoverDimension(altDimA, altDimB, altDimC));
	        
	        // Vary dimension C only
	        fallbacks.add(new SpaCoverDimension(altDimA, altDimB, altDimC + 1));
	        fallbacks.add(new SpaCoverDimension(altDimA, altDimB, altDimC - 1));
	        
	        // Vary dimension A only
	        fallbacks.add(new SpaCoverDimension(altDimA + 1, altDimB, altDimC));
	        fallbacks.add(new SpaCoverDimension(altDimA - 1, altDimB, altDimC));
	        
	        // Vary A and C
	        fallbacks.add(new SpaCoverDimension(altDimA + 1, altDimB, altDimC + 1));
	        fallbacks.add(new SpaCoverDimension(altDimA - 1, altDimB, altDimC + 1));
	        fallbacks.add(new SpaCoverDimension(altDimA + 1, altDimB, altDimC - 1));
	        fallbacks.add(new SpaCoverDimension(altDimA - 1, altDimB, altDimC - 1));
	        
	        // Vary dimension B only
	        fallbacks.add(new SpaCoverDimension(altDimA, altDimB + 1, altDimC));
	        fallbacks.add(new SpaCoverDimension(altDimA, altDimB - 1, altDimC));
	        
	        // Vary B and C
	        fallbacks.add(new SpaCoverDimension(altDimA, altDimB + 1, altDimC + 1));
	        fallbacks.add(new SpaCoverDimension(altDimA, altDimB - 1, altDimC + 1));
	        fallbacks.add(new SpaCoverDimension(altDimA, altDimB + 1, altDimC - 1));
	        fallbacks.add(new SpaCoverDimension(altDimA, altDimB - 1, altDimC - 1));
	      
	        // Vary A and B
	        fallbacks.add(new SpaCoverDimension(altDimA + 1, altDimB + 1, altDimC));
	        fallbacks.add(new SpaCoverDimension(altDimA - 1, altDimB - 1, altDimC));
	        fallbacks.add(new SpaCoverDimension(altDimA + 1, altDimB - 1, altDimC));
	        fallbacks.add(new SpaCoverDimension(altDimA - 1, altDimB + 1, altDimC));
	        
	        // Vary all three dimensions
	        fallbacks.add(new SpaCoverDimension(altDimA + 1, altDimB + 1, altDimC + 1));
	        fallbacks.add(new SpaCoverDimension(altDimA - 1, altDimB - 1, altDimC + 1));
	        fallbacks.add(new SpaCoverDimension(altDimA + 1, altDimB - 1, altDimC + 1));
	        fallbacks.add(new SpaCoverDimension(altDimA - 1, altDimB + 1, altDimC + 1));
	        fallbacks.add(new SpaCoverDimension(altDimA + 1, altDimB + 1, altDimC - 1));
	        fallbacks.add(new SpaCoverDimension(altDimA - 1, altDimB - 1, altDimC - 1));
	        fallbacks.add(new SpaCoverDimension(altDimA + 1, altDimB - 1, altDimC - 1));
	        fallbacks.add(new SpaCoverDimension(altDimA - 1, altDimB + 1, altDimC - 1));
	        
	        return fallbacks;
	    }

	  
	    /**
	     * Generate SKU array from dimension list
	     */
	    public static List<String> generateSKUArray(List<SpaCoverDimension> dimensions) {
	        List<String> skuArray = new ArrayList<>();
	        for (SpaCoverDimension dim : dimensions) {
	            String sku = DimensionConverter.convertToSKU(dim);
	            skuArray.add(sku);
	        }
	        return skuArray;
	    }
	
}
