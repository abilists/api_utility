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

		String str = null;
		JSONObject jSONObject = null;
		try {
			Map<String, String> headersMap = new HashMap<String, String>();
			headersMap.put("Content-Type", "application/json; charset=utf-8");
			headersMap.put("Authorization", "Basic base64_encoding");

			String params = "{\"test\":\"value\"}"; 
			headersMap.put("Content-Length", Integer.toString(params.length()));

			// str = ApiHttpsClient.httpsClientTxt("https://test.co.kr/api", headersMap, ApiHttpsClient.POST, params);

			jSONObject = ApiHttpsClient.httpsClient("https://test.co.kr/api", headersMap, ApiHttpsClient.POST, params);
			
			System.out.println("str = " + jSONObject.toJSONString());

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
