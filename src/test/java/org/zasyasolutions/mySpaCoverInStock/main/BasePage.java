package org.zasyasolutions.mySpaCoverInStock.main;

import java.lang.reflect.Method;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;


import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

public class BasePage {

	 protected static String token;
	 protected static String authToken;
	 public RequestSpecification requestSpec;
	 
	 public static String baseUrl;
	 public static String inventoryEndpoint;

	    @BeforeClass
	    public void initializeBasePage() {

	        this.baseUrl = ConfigReader.getProperty("base.url");
	        this.inventoryEndpoint = ConfigReader.getProperty("inventory.endpoint");
	        RestAssured.baseURI = baseUrl;
	        output("Base URL configured: " +  RestAssured.baseURI);
	    }
	    
	 
	 
	 
	 public  String baseURI() {
		 return RestAssured.baseURI = this.baseUrl;
	 }
	 public void output(Object value) {
		 System.out.println(value);
	 }

	    public static void setToken(String authToken) {
	        token = authToken;
	    }

	 
	    public static String getToken() {
	    	return token;
	    }
	
}
