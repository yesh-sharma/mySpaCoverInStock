package org.zasyasolutions.mySpaCoverInStock.tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
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

	        // 1️⃣ Read CSV
	        List<SpaCoverDimension> csvDimensions =
	                CsvDimensionReader.readDimensions("src/test/resources/dimensions.csv");

	        // 2️⃣ Apply fallback logic
	        List<SpaCoverDimension> allFallbackDimensions = new ArrayList<>();
	        for (SpaCoverDimension dim : csvDimensions) {
	            allFallbackDimensions.addAll(
	                FallbackDimensionGenerator.generateFallbacks(dim)
	            );
	        }

	        // 3️⃣ Convert fallback dimensions → SKU array
	        List<String> skuArray =
	                FallbackDimensionGenerator.generateSKUArray(allFallbackDimensions);

	        // ✅ Now skuArray contains SKUs with first digit conversion applied automatically
	        System.out.println("Generated SKUs: " + skuArray);

	        // 4️⃣ Send SKU array as payload in API call
	        Map<String, Object> payload = new HashMap<>();
	        payload.put("skus", skuArray);

	        Response response = given()
	                .contentType(ContentType.JSON)
	                .body(payload)
	                .when()
	                .post("/inventory/search")
	                .then()
	                .statusCode(200)
	                .extract()
	                .response();

	        System.out.println("API Response: " + response.asString());
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
