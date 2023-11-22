package io.utility.api;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64.Encoder;

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

	@Test
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

	// @Test
	public void testTwo() throws Exception {

		JSONObject jSONObject = null;
		try {
			String strTest = "97f3c717da19b4697ee9884e67aabce6:56b2d2400f5410f64e37c14d11150e3d75e3b9c7ccc76fb8cf19d912990f8994";
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
