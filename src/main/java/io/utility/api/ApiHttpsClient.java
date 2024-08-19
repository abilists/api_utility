package io.utility.api;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
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

	public static String httpsClientTxt(String url, Map<String, String> requestHeaders, String method, String params) throws Exception {

		HttpsURLConnection httpsURLConn = httpsClientConnection(url, requestHeaders, method);

		InputStream is = null;
		InputStreamReader streamReader = null;
		try {
			if(params != null) {
				OutputStream os = httpsURLConn.getOutputStream();
				os.write(params.getBytes("UTF-8"));
				os.flush();
			}

			is = httpsURLConn.getInputStream();
			if(is == null) {
				logger.error("InputStream is null.");
				return null;
			}
			streamReader = new InputStreamReader(is, "UTF-8");

		    int data = streamReader.read();
		    StringBuffer sb = new StringBuffer();
		    while(data != -1) {
		        char theChar = (char) data;
		        sb.append(theChar);
		        data = streamReader.read();
		    }

			return sb.toString();
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
	
	public static HttpsURLConnection httpsClientConnection(String url, Map<String, String> requestHeaders, String method) throws Exception {

		URL pickUrl = new URL(url);
		HttpsURLConnection httpsURLConn = (HttpsURLConnection)pickUrl.openConnection();
		if(method == null || method.length() < 1) {
			httpsURLConn.setRequestMethod(GET);
		} else {
			httpsURLConn.setRequestMethod(POST);
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
		httpsURLConn.setDoOutput(true);
		httpsURLConn.setDoInput(true);
		httpsURLConn.setUseCaches(false);

		return httpsURLConn;
	}

	public static JSONObject httpsClientJsonObj(HttpsURLConnection httpsURLConn, String params) throws Exception {

		InputStream is = null;
		InputStreamReader streamReader = null;
		JSONObject jSONObject = null;
		try {
			if(params != null) {
				OutputStream os = httpsURLConn.getOutputStream();
				os.write(params.getBytes("UTF-8"));
				os.flush();
			}

			is = httpsURLConn.getInputStream();
			if(is == null) {
				logger.error("InputStream is null.");
				return null;
			}
			streamReader = new InputStreamReader(is, "UTF-8");

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

	public static JSONObject httpsClient(String url, Map<String, String> requestHeaders, String method, String params) throws Exception {

		URL pickUrl = new URL(url);
		HttpsURLConnection httpsURLConn = (HttpsURLConnection)pickUrl.openConnection();
		if(method == null || method.length() < 1) {
			httpsURLConn.setRequestMethod(GET);
		} else {
			httpsURLConn.setRequestMethod(POST);
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
		httpsURLConn.setDoOutput(true);
		httpsURLConn.setDoInput(true);
		httpsURLConn.setUseCaches(false);
		// Get the response and parse it into another JSON object which are the
		//'user attributes'.
		// This example uses UTF-8 if encoding is not found in request.
		// String encoding = httpsURLConn.getContentEncoding();

		InputStream is = null;
		InputStreamReader streamReader = null;
		JSONObject jSONObject = null;
		try {
			if(params != null) {
				OutputStream os = httpsURLConn.getOutputStream();
				os.write(params.getBytes("UTF-8"));
				os.flush();
			}

			is = httpsURLConn.getInputStream();
			if(is == null) {
				logger.error("InputStream is null.");
				return null;
			}
			streamReader = new InputStreamReader(is, "UTF-8");

			// Debugging
			if(logger.isDebugEnabled()) {
			    int data = streamReader.read();
			    StringBuffer sb = new StringBuffer();
			    while(data != -1) {
			        char theChar = (char) data;
			        sb.append(theChar);
			        data = streamReader.read();
			    }
			    logger.debug("reponse = " + sb.toString());
			}
			// If logger is debug mode, streamReader is null.
			if(!logger.isDebugEnabled()) {
				JSONParser parser = new JSONParser();
				jSONObject = (JSONObject)parser.parse(streamReader);
			}
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

	public JSONObject getAccessToken(String contestType, String authorization, String url) throws Exception {
		JSONObject jSONObject = null;
		try {
			Map<String, String> headersMap = new HashMap<String, String>();
			headersMap.put("Content-Type", contestType);
			headersMap.put("Authorization", authorization); // ID:PWD
			jSONObject = ApiHttpsClient.httpsClient(url, headersMap, ApiHttpsClient.POST, null);
        } catch (Exception e) {
        	logger.error("getAccessToken : Exception error", e);
        }

		return jSONObject;
	}

}