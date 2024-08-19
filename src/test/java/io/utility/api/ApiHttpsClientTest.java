package io.utility.api;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ApiHttpsClientTest {

	OpenIdConnect sslHttpsClient;

	@BeforeClass
	public static void beforeClass() {
		System.out.println("This is the first excuted");
	}

	@Before
	public void before() {
		System.out.println("Before");
	}

	// @Test
	public void testOne() throws Exception {

		JSONObject jSONObject = null;
		try {
			Map<String, String> headersMap = new HashMap<String, String>();
			headersMap.put("Content-Type", "application/json; charset=utf-8");
			headersMap.put("Authorization", "Basic base64_encoding");

			String params = "{\"test\":\"value\"}"; 
			headersMap.put("Content-Length", Integer.toString(params.length()));

			jSONObject = ApiHttpsClient.httpsClient("https://localhost:8443/api/oauth", headersMap, ApiHttpsClient.POST, params);
			
			System.out.println("str = " + jSONObject.toJSONString());
        } catch (Exception e) {
        	System.out.println("getAccessToken : Exception error " + e);
        }

	}

	@Test
	public void testTwo() throws Exception {

		JSONObject jSONObject = null;
		try {
			String strTest = "24b320f630b480985fb6793da9f50d34:c8e9aaa219f36426afa828a6eac88876";
			String encoded = Base64.getEncoder().encodeToString(strTest.getBytes());
			System.out.println("encoded = " + encoded);
        } catch (Exception e) {
        	System.out.println("getAccessToken : Exception error " + e);
        }

	}

	// @Test
	public void testThree() throws Exception {

		JSONObject jSONObject = null;
		try {
			String strTest = "97f3c717da19b4697ee9884e67aabce6:d11dc0e97d1fe3bceeea041a1dd10859";
			String encoded = Base64.getEncoder().encodeToString(strTest.getBytes());
			System.out.println("encoded = " + encoded);
        } catch (Exception e) {
        	System.out.println("getAccessToken : Exception error " + e);
        }

	}

	@After
	public void after() {
		System.out.println("Before");
	}

	@AfterClass
	public static void afterClass() {
		System.out.println("This is the end excuted");
	}
}
