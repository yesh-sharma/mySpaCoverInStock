package org.zasyasolutions.mySpaCoverInStock.utils;

import org.zasyasolutions.mySpaCoverInStock.model.SpaCoverDimension;

public class DimensionConverter {

	
	    public static String replaceFirstDigit(String dimension) {
	        if (dimension == null || dimension.isEmpty()) {
	            return dimension;
	        }

	        char firstChar = dimension.charAt(0);
	        String replacement;

	        switch (firstChar) {
	            case '6':
	                replacement = "X";
	                break;
	            case '7':
	                replacement = "S";
	                break;
	            case '8':
	                replacement = "E";
	                break;
	            case '9':
	                replacement = "N";
	                break;
	            default:
	                replacement = String.valueOf(firstChar);
	                break;
	        }

	        if (dimension.length() > 1) {
	            return replacement + dimension.substring(1);
	        } else {
	            return replacement;
	        }
	    }

	    /**
	     * Converts dimension to SKU format
	     */
	    public static String convertToSKU(int dimA, int dimB, int dimC) {
	        String skuA = replaceFirstDigit(String.valueOf(dimA));
	        String skuB = replaceFirstDigit(String.valueOf(dimB));

	       
	            return skuA + skuB + "-" + dimC;
	        
	    }

	    /**
	     * Converts SpaCoverDimension object to SKU
	     */
	    public static String convertToSKU(SpaCoverDimension dimension) {
	       
	        return convertToSKU(dimension.getDimensionA(), 
	                           dimension.getDimensionB(), 
	                           dimension.getDimensionC() 
	                          );
	    }
	
	
}
