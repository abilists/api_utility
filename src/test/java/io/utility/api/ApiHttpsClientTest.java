package io.utility.api;

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
			jSONObject = ApiHttpsClient.httpsClient("https://msp.f-secure.com/web-test/common/test.html", null, ApiHttpsClient.GET);
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
