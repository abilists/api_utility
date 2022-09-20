package io.utility.api;

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

	@Test
	public void testOne() throws Exception {
		JSONObject jSONObject = null;
		try {			
			Map<String, String> headersMap = new HashMap<String, String>();
			headersMap.put("Content-Type", "application/json; charset=utf-8");
			headersMap.put("Authorization", "Basic base64");

			jSONObject = ApiHttpsClient.httpsClient("https://test.com/v1/token", headersMap, ApiHttpsClient.POST, null);
			System.out.println(jSONObject.toString());
		} catch (Exception e) {
			e.printStackTrace();
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
