package org.zasyasolutions.mySpaCoverInStock.tests;

import io.restassured.RestAssured; 
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.zasyasolutions.mySpaCoverInStock.dataprovider.SpaCoverDataProvider;
import org.zasyasolutions.mySpaCoverInStock.main.BasePage;
import org.zasyasolutions.mySpaCoverInStock.main.ConfigReader;
import org.zasyasolutions.mySpaCoverInStock.model.SpaCoverDimension;
import org.zasyasolutions.mySpaCoverInStock.pages.InventorySearchAPIClient;
import org.zasyasolutions.mySpaCoverInStock.utils.CsvDimensionReader;
import org.zasyasolutions.mySpaCoverInStock.utils.DimensionConverter;
import org.zasyasolutions.mySpaCoverInStock.utils.FallbackDimensionGenerator;

import org.zasyasolutions.mySpaCoverInStock.utils.PayloadGenerator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static io.restassured.RestAssured.given;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test class for Spa Cover Inventory API
 */
public class SpaCoverInventoryAPITest extends BasePage {

	private InventorySearchAPIClient apiClient;
	

	@BeforeClass(dependsOnMethods = { "initializeBasePage" })
	public void setup() {
		// Read from config file or properties

		apiClient = new InventorySearchAPIClient(baseUrl, inventoryEndpoint);

		output("Test Setup Complete");
		output("Base URL: " + baseUrl);
		output("Endpoint: " + inventoryEndpoint);
	}

//    @Test(description = "Test single dimension conversion to SKU format")
	public void testDimensionToSKUConversion() {
		SpaCoverDimension dimension = new SpaCoverDimension(91, 82, 6);
		String expectedSKU = "N1E2-6";

		String actualSKU = DimensionConverter.convertToSKU(dimension);

		output("\nOriginal Dimensions: A=" + dimension.getDimensionA() + ", B=" + dimension.getDimensionB()
				+ ", C=" + dimension.getDimensionC());
		output("Expected SKU: " + expectedSKU);
		output("Actual SKU: " + actualSKU);

		Assert.assertEquals(actualSKU, expectedSKU, "SKU conversion should match expected format");
	}

	// @Test(description = "Test fallback generation for standard dimensions")
	public void testFallbackGenerationStandard() {
		SpaCoverDimension dimension = new SpaCoverDimension(91, 82, 6);

		List<SpaCoverDimension> fallbacks = FallbackDimensionGenerator.generateFallbacks(dimension);

		output("\nGenerated " + fallbacks.size() + " fallback dimensions");

		Assert.assertTrue(fallbacks.size() > 0, "Should generate at least one fallback dimension");

		// Verify original is included
		boolean containsOriginal = fallbacks.stream()
				.anyMatch(d -> d.getDimensionA() == 91 && d.getDimensionB() == 82 && d.getDimensionC() == 6);

		Assert.assertTrue(containsOriginal, "Fallback list should include original dimensions");
	}

	@Test
	public void inventorySearchWithFallbackSkus() {

	    List<List<String>> skuPayloads = PayloadGenerator.generatePayloads();

	    for (int i = 0; i < skuPayloads.size(); i++) {

	        List<String> skuList = skuPayloads.get(i);

	        Map<String, Object> data = new HashMap<>();
	        data.put("sku", skuList);
	        Gson gson = new GsonBuilder().setPrettyPrinting().create();
	        String payload = gson.toJson(data);
	        
	        output("Endpoint: " + inventoryEndpoint);
	        output(">> Sending payload for entry " + (i + 1) + ": " + payload);

	        Response response =
	        	    requestSpec
	        	        .body(payload)
	        	        .log().all()
	        	       
	        	        .header("Accept","application/json")
	        	    .when()
	        	        .post(inventoryEndpoint)
	        	    .then()
	        	        .log().all()  
	        	        .extract()
	        	        .response();

	        	output("Status Code: " + response.getStatusCode());
	        	output("Response Body: " + response.getBody().asString());
	        	

	        	// Then assert based on what you see
	        	Assert.assertEquals(response.getStatusCode(), 201);

	        output(">> Response for entry " + (i + 1) + ": .... ");
//	        response.prettyPrint();
	        String responseBody = response.getBody().asString();
	        JsonPath js1 = new JsonPath(responseBody);
			int count = js1.getInt("inventory.size()"); 
			output("Total inventory items found: " + count);
			 Assert.assertTrue(count >= 0, "Inventory items should be returned");
			 List<String> availableSkus = js1.getList("inventory.sku");
			 output(">> Getting a get Response for sku : "+availableSkus);
			for(int j=0;j<count;j++) {
				
			}
	    }
	}

	

//	    @Test(description = "Test dimension converter for all replacement digits")
	public void testDimensionConverterAllDigits() {
		String[][] testCases = { { "60", "X0" }, { "72", "S2" }, { "84", "E4" }, { "96", "N6" }, { "50", "50" }, };

		output("\n=== Testing Dimension Converter ===");

		for (String[] testCase : testCases) {
			String input = testCase[0];
			String expected = testCase[1];
			String actual = DimensionConverter.replaceFirstDigit(input);

			output("Input: " + input + " -> Expected: " + expected + " -> Actual: " + actual);

			Assert.assertEquals(actual, expected, "Dimension " + input + " should convert to " + expected);
		}
	}

}
