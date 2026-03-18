package org.zasyasolutions.mySpaCoverInStock.tests;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.zasyasolutions.mySpaCoverInStock.main.BasePage;
import org.zasyasolutions.mySpaCoverInStock.pages.InventorySearchAPIClient;
import org.zasyasolutions.mySpaCoverInStock.utils.CircleInventoryResponseOrganizer;
import org.zasyasolutions.mySpaCoverInStock.utils.CirclePayloadGenerator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpaCoverCircleInventoryAPITest extends BasePage {

    private InventorySearchAPIClient apiClient;

    @BeforeClass(dependsOnMethods = { "initializeBasePage" })
    public void setup() {
        apiClient = new InventorySearchAPIClient(baseUrl, inventoryEndpoint);

        output("Circle Test Setup Complete");
        output("Base URL: " + baseUrl);
        output("Endpoint: " + inventoryEndpoint);
    }

    @Test
    public void circleInventorySearchWithFallbackSkus() {

        List<List<String>> skuPayloads = CirclePayloadGenerator.generateCirclePayloads();

        String outputFilePath = "circle_inventory_report_" + System.currentTimeMillis() + ".csv";
        output("Circle CSV output file will be saved to: " + outputFilePath);

        CircleInventoryResponseOrganizer.resetFileState();

        for (int i = 0; i < skuPayloads.size(); i++) {

            List<String> skuList = skuPayloads.get(i);

            Map<String, Object> data = new HashMap<>();
            data.put("sku", skuList);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String payload = gson.toJson(data);

            output("Endpoint: " + inventoryEndpoint);
            output(">> Sending circle payload for entry " + (i + 1) + ": " + payload);

            Response response =
                requestSpec
                    .body(payload)
                    .log().all()
                    .header("Accept", "application/json")
                .when()
                    .post(inventoryEndpoint)
                .then()
                    .log().all()
                    .extract()
                    .response();

            output("Status Code: " + response.getStatusCode());
            Assert.assertEquals(response.getStatusCode(), 201);

            output(">> Response for entry " + (i + 1) + ": .... ");

            String responseBody = response.getBody().asString();
            JsonPath js1 = new JsonPath(responseBody);
            int count = js1.getInt("inventory.size()");
            output("Total inventory items found: " + count);
            Assert.assertTrue(count >= 0, "Inventory items should be returned");

            List<String> availableInventorySkus = js1.getList("inventory.sku");
            List<String> availableInboundSkus = js1.getList("inbound.sku");
            List<String> availableCustomSkus = js1.get("custom") != null
                ? js1.getList("custom.sku")
                : new ArrayList<>();

            output(">> Getting a get Response for inventory sku : " + availableInventorySkus);
            output(">> Getting a get Response for inbound sku : " + availableInboundSkus);
            output(">> Getting a get Response for custom sku : " + availableCustomSkus);

            try {
                output("\n>> Processing and organizing circle inventory for payload " + (i + 1));
                CircleInventoryResponseOrganizer.processAndSaveInventory(
                    responseBody,
                    skuList,
                    outputFilePath
                );
                output(">> Circle inventory organized successfully for payload " + (i + 1));
            } catch (Exception e) {
                output("!! Error organizing circle inventory: " + e.getMessage());
                e.printStackTrace();
            }
        }

        output("\n" + "=".repeat(80));
        output("OK: CIRCLE TEST COMPLETED - INVENTORY REPORT SAVED TO: " + outputFilePath);
        output("=".repeat(80));
    }
}
