package org.zasyasolutions.mySpaCoverInStock.utils;

import java.util.ArrayList;
import java.util.List;

import org.zasyasolutions.mySpaCoverInStock.model.SpaCoverDimension;

public class FallbackDimensionGenerator {

    /**
     * Generate all fallback dimension combinations
     */
//    public static List<SpaCoverDimension> generateFallbacks(SpaCoverDimension original) {
//
//        List<SpaCoverDimension> fallbackAttempts = new ArrayList<>();
//
//        int altDimA = original.getDimensionA();
//        int altDimB = original.getDimensionB();
//        int altDimC = original.getDimensionC();
//
//        fallbackAttempts.addAll(generateStandardFallbacksNonProduct(altDimA, altDimB, altDimC));
//
//        return fallbackAttempts;
//    }

	
	
	public static List<SpaCoverDimension> generateFallbacks(SpaCoverDimension original) {

	    List<SpaCoverDimension> fallbackAttempts = new ArrayList<>();

	    int altDimA = original.getDimensionA();
	    int altDimB = original.getDimensionB();
	    int altDimC = original.getDimensionC();

	    fallbackAttempts.addAll(
	        generateStandardFallbacksNonProduct(altDimA, altDimB, altDimC)
	    );

	    // Put original dimension at 0th index
	    fallbackAttempts.add(0, original);

	    return fallbackAttempts;
	}

	
	
	
	
    /**
     * Non-product fallback logic using permutations,
     * excluding exact forbidden offset combinations.
     */
//    private static List<SpaCoverDimension> generateStandardFallbacksNonProduct(
//            int altDimA, int altDimB, int altDimC) {
//
//        List<SpaCoverDimension> fallbacks = new ArrayList<>();
//
//        // Define algebraic offsets
//        int[] aOffsets = {-1, 0, 1, 2};
//        int[] bOffsets = {-1, 0, 1, 2};
//        int[] cOffsets = {-1, 0, 1};
//
//        // Forbidden offset combinations (exact matches to exclude)
//        int[][] forbidden = {
//            {-1,  2, -1},
//            {-1,  2,  0},
//            { 2, -1, -1},
//            {-1,  0,  1},
//            { 2, -1,  0},
//            { 2, -1,  1}
//        };
//
//        for (int aOff : aOffsets) {
//            for (int bOff : bOffsets) {
//                for (int cOff : cOffsets) {
//
//                    // Skip if this exact combination is forbidden
//                    boolean isForbidden = false;
//                    for (int[] f : forbidden) {
//                        if (aOff == f[0] && bOff == f[1] && cOff == f[2]) {
//                            isForbidden = true;
//                            break;
//                        }
//                    }
//                    if (isForbidden) continue; // skip only this combination
//
//                    int newA = altDimA + aOff;
//                    int newB = altDimB + bOff;
//                    int newC = altDimC + cOff;
//
//                    // Skip invalid dimensions
//                    if (newA <= 0 || newB <= 0 || newC <= 0) continue;
//
//                    fallbacks.add(new SpaCoverDimension(newA, newB, newC));
//                }
//            }
//        }
//
//        return fallbacks;
//    }
	
	
	private static List<SpaCoverDimension> generateStandardFallbacksNonProduct(
            int altDimA, int altDimB, int altDimC) {

        List<SpaCoverDimension> fallbacks = new ArrayList<>();

        // Define algebraic offsets
        int[] aOffsets = {-1, 0, 1, 2};
        int[] bOffsets = {-1, 0, 1, 2};
        
        // Conditional C offsets based on altDimC value
        int[] cOffsets;
        if (altDimC == 0) {
            cOffsets = new int[]{0, 1};  // When C=0, only allow 0 and +1
        } else {
            cOffsets = new int[]{-1, 0, 1};  // When C>0, allow -1, 0, +1
        }

        // Forbidden offset combinations (exact matches to exclude)
        int[][] forbidden = {
            {-1,  2, -1},
            {-1,  2,  0},
            { 2, -1, -1},
            {-1,  0,  1},
            { 2, -1,  0},
            { 2, -1,  1}
        };

        for (int aOff : aOffsets) {
            for (int bOff : bOffsets) {
                for (int cOff : cOffsets) {

                    // Skip if this exact combination is forbidden
                    boolean isForbidden = false;
                    for (int[] f : forbidden) {
                        if (aOff == f[0] && bOff == f[1] && cOff == f[2]) {
                            isForbidden = true;
                            break;
                        }
                    }
                    if (isForbidden) continue;

                    // Calculate new dimensions
                    int newA = altDimA + aOff;
                    int newB = altDimB + bOff;
                    int newC = altDimC + cOff;

                    // Skip invalid dimensions (A and B must be positive, C can be 0 or positive)
                    if (newA <= 0 || newB <= 0 || newC < 0) continue;

                    fallbacks.add(new SpaCoverDimension(newA, newB, newC));
                }
            }
        }

        return fallbacks;
    }
	
	
	
	
	
	
	
	
	
	
	

    /**
     * Generate SKU array from dimension list
     */
    public static List<String> generateSKUArray(List<SpaCoverDimension> dimensions) {
        List<String> skuArray = new ArrayList<>();
        for (SpaCoverDimension dim : dimensions) {
            skuArray.add(DimensionConverter.convertToSKU(dim));
        }
        return skuArray;
    }
}
