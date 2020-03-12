package io.utility.api;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiHttpUrl {

	private static Logger logger = LoggerFactory.getLogger(ApiHttpUrl.class);

	public static String GET = "GET";
	public static String POST = "POST";

	public static String reqPostMethod() throws Exception {

	    HttpURLConnection conn = (HttpURLConnection) new URL("https://www.google.com/recaptcha/api/siteverify").openConnection();
	    String params = "secret=bbbbbbbbbbbbbbbbbbbbbbbbbbbbb" + "&response=" + "aaaaaaaaaaaaaaaaaaaaaaaaa";
	    conn.setRequestMethod("POST");
	    conn.setDoOutput(true);
	    DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
	    wr.writeBytes(params);
	    wr.flush();
	    wr.close();

	    int responseCode = conn.getResponseCode();
	    StringBuffer responseBody = new StringBuffer();
	    if (responseCode == 200) {
	        BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
	        BufferedReader reader = new BufferedReader(new InputStreamReader(bis));
	        String line;
	        while ((line = reader.readLine()) != null) {
	            responseBody.append(line);
	        }
	        bis.close();
	        
	        logger.info(responseBody.toString());
	    }

		return null;

	}
}
