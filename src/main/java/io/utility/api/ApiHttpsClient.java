package io.utility.api;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiHttpsClient {

	private static Logger logger = LoggerFactory.getLogger(ApiHttpsClient.class);

	public static String GET = "GET";
	public static String POST = "POST";

	public static JSONObject httpsClient(String url, Map<String, String> requestHeaders, String method) throws Exception {

		URL pickUrl = new URL(url);
		HttpsURLConnection httpsURLConn = (HttpsURLConnection)pickUrl.openConnection();
		if(method == null || method.length() < 1) {
			httpsURLConn.setRequestMethod(GET);
		} else {
			httpsURLConn.setRequestMethod(method);
		}

		// True to verify certificate 
		final HostnameVerifier hv=new HostnameVerifier() {
			@Override
			public boolean verify(String arg0, SSLSession arg1) {
				// TODO Auto-generated method stub
				return true;
			}
		};
		httpsURLConn.setHostnameVerifier(hv);

		// Create an SSLContext that uses our TrustManager
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, null, null);
		httpsURLConn.setSSLSocketFactory(sslContext.getSocketFactory());
		if(requestHeaders != null) {
			for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
				httpsURLConn.setRequestProperty(entry.getKey(), entry.getValue());
			}
		}

		httpsURLConn.setInstanceFollowRedirects(true);
		// Get the response and parse it into another JSON object which are the
		//'user attributes'.
		// This example uses UTF-8 if encoding is not found in request.
		String encoding = httpsURLConn.getContentEncoding();

		InputStream is = null;
		InputStreamReader streamReader = null;
		JSONObject jSONObject = null;

		try {
			is = httpsURLConn.getInputStream();
			if(is == null) {
				logger.error("InputStream is null.");
				return null;
			}
			streamReader = new InputStreamReader(is, encoding != null ? encoding : "UTF-8");

			// Debugging
			if(logger.isDebugEnabled()) {
			    int data = streamReader.read();
			    StringBuffer sb = new StringBuffer();
			    while(data != -1) {
			        char theChar = (char) data;
			        sb.append(theChar);
			        data = streamReader.read();
			    }
			    logger.debug(sb.toString());
			}

			JSONParser parser = new JSONParser();
			jSONObject = (JSONObject)parser.parse(streamReader);

			return jSONObject;
		} catch (Exception e) {
			logger.error("Exception error", e);
			throw e;
		} finally {
			if(is != null) {
				is.close();
			}
			if(streamReader != null) {
				streamReader.close();
			}
		}

	}

}
