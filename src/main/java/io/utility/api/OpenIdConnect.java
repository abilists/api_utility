package io.utility.api;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenIdConnect {

	private static final Logger logger = LoggerFactory.getLogger(OpenIdConnect.class);

	public static String CERTIFICATE_KEY = "key";

	private String httpsUrl = "";

	// True to verify certificate 
	private HostnameVerifier hv = new HostnameVerifier() {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			// Ignore host name verification. It always returns true. 
			return true;
		}
	};

	private InputStreamReader connectHttps(TrustManagerFactory tmf, Map<String, String> mapPara, int connectTimeOut, int readTimeOut) throws Exception {
	
		if(!checkParameters()) {
			return null;
		}
	
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, tmf.getTrustManagers(), null);
	
		SSLSocketFactory socketFactory = sslContext.getSocketFactory();
	
		URL pickUrl = new URL(httpsUrl);
		URLConnection urlConn = pickUrl.openConnection();
		urlConn.setConnectTimeout(connectTimeOut);
	    urlConn.setReadTimeout(readTimeOut);

	    // Set a verifier and a socket factory.
		HttpsURLConnection httpsURLConn = (HttpsURLConnection)urlConn;
		httpsURLConn.setHostnameVerifier(hv);
		httpsURLConn.setSSLSocketFactory(socketFactory);

		// Set parameters.
		for(String key : mapPara.keySet()) {
			urlConn.setRequestProperty(key, mapPara.get(key));
		}
	
		// Get the response and parse it into another JSON object which are the
		// 'user attributes'.
		// This example uses UTF-8 if encoding is not found in request.
		String encoding = urlConn.getContentEncoding();

		if (encoding == null || encoding.length() < 1) {
			encoding = "UTF-8";
		}

		try(InputStream is = urlConn.getInputStream();
			InputStreamReader streamReader = new InputStreamReader(is, encoding);
		) {
			return streamReader;
		} catch (Exception e) {
			logger.error("A error occur, when getting the response. ", e);
			return null;
		}


	}

	public boolean checkParameters() {
		if(this.httpsUrl == null || this.httpsUrl.length() < 1) {
			return false;
		}
		return true;
	}

	public File getFile(String path) {

		// Check, if a file is
		File file = new File(path);
		if(!file.exists()) {
			logger.error("There is no file. path=" + path);
			return null;
		}

		return file;
	}

	public Certificate getCertificate(File file) {
		Certificate ca;
		try (InputStream caInput = new BufferedInputStream(new FileInputStream(file))) {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			ca = cf.generateCertificate(caInput);
		} catch (Exception e) {
			logger.error("The certificate file is bad.");
			return null;
		}

		return ca;

	}

	public KeyStore getKeyStore(Certificate ca) throws Exception {
		return getKeyStore(ca, null, null);
	}

	public KeyStore getKeyStore(Certificate ca, InputStream is, char[] password) throws Exception {

		String keyStoreType = KeyStore.getDefaultType();
		KeyStore keyStore = KeyStore.getInstance(keyStoreType);
		// Parameters are null. because the keyStore is the default.
		keyStore.load(null, null);
		keyStore.setCertificateEntry(CERTIFICATE_KEY, ca);

		return keyStore;
	}

	public TrustManagerFactory getTrustManager(KeyStore keyStore) throws Exception {

		// Create a TrustManager that trusts the CAs in our KeyStore
		String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
		tmf.init(keyStore);
		
		return tmf;
	}

	public JSONObject connectHttpsJson(TrustManagerFactory tmf) throws Exception {
		return connectHttpsJson(tmf, null, 10000, 10000);
	}

	public JSONObject connectHttpsJson(TrustManagerFactory tmf, Map<String, String> mapPara) throws Exception {
		return connectHttpsJson(tmf, mapPara, 10000, 10000);
	}

	public JSONObject connectHttpsJson(TrustManagerFactory tmf, Map<String, String> mapPara, int connectTimeOut, int readTimeOut) throws Exception {

		JSONObject dataJson = null;
		try(InputStreamReader streamReader = connectHttps(tmf, mapPara, connectTimeOut, readTimeOut)) {
			JSONParser parser = new JSONParser();
			dataJson = (JSONObject)parser.parse(streamReader);
		} catch (Exception e) {
			logger.error("A error occur, when getting the response.", e);
		}

		return dataJson;
	}

	public void setHostnameVerifier(final HostnameVerifier hv) {
		this.hv = hv;
	}

	public void setHttpsUrl(String httpsUrl) {
		this.httpsUrl = httpsUrl;
	}

	public void setCertificateKey(String certificateKey) {
		CERTIFICATE_KEY = certificateKey;
	}

}