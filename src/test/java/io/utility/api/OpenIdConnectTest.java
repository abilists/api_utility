package io.utility.api;

import java.io.File;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.TrustManagerFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class OpenIdConnectTest {

	OpenIdConnect sslHttpsClient;

	@BeforeClass
	public static void beforeClass() {
		System.out.println("This is the first excuted");
	}

	@Before
	public void before() {
		System.out.println("Before");
		sslHttpsClient = new OpenIdConnect();
	}

	@Test
	public void testOne() throws Exception {

		try {

			// Request URL.
			String strResponseValue = "valueFromProviderServer";

			// ---------------------------------------------
			//
			// Set a key
			sslHttpsClient.setCertificateKey("key");
			// Set a URL.
			sslHttpsClient.setHttpsUrl("https://sample.com/sample1?aaa=" + strResponseValue);

			// A path of certification file.
			File file = sslHttpsClient.getFile("/usr/local/test1");
			Certificate ca = sslHttpsClient.getCertificate(file);
			KeyStore ks = sslHttpsClient.getKeyStore(ca);
			TrustManagerFactory tf = sslHttpsClient.getTrustManager(ks);

			Map<String, String> mapPara = new HashMap<String, String>();
			mapPara.put("id", "sample");
			mapPara.put("pwd", "password");
			mapPara.put("para1", "parameter1");
			sslHttpsClient.connectHttpsJson(tf,  mapPara);

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("This is the test");
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
