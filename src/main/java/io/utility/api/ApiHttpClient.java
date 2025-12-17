package io.utility.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for handling HTTP and HTTPS requests using Apache HttpClient.
 * <p>
 * This class provides convenient wrappers for common HTTP operations including:
 * <ul>
 *   <li>GET and POST requests</li>
 *   <li>JSON body handling</li>
 *   <li>File uploads and downloads</li>
 *   <li>OAuth token retrieval</li>
 * </ul>
 * </p>
 */
public class ApiHttpClient {

	private static final Logger logger = LoggerFactory.getLogger(ApiHttpClient.class);

	public static final String GET = "GET";
	public static final String POST = "POST";
	
	private static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8";
	private static final String CONTENT_TYPE_TEXT_JSON = "text/json;charset=UTF-8";
	private static final String CONTENT_TYPE_XML = "text/xml;charset=utf-8";
	private static final String ENCODING_UTF8 = "UTF-8";

	/**
	 * Converts an InputStreamReader to a String.
	 * 
	 * @param inputStreamReader The input stream reader to read from.
	 * @return The content as a String, or null if the input is null.
	 * @throws IOException If an I/O error occurs.
	 */
	public static String convert(InputStreamReader inputStreamReader) throws IOException {

		if (inputStreamReader == null) {
			logger.error("Input stream reader is null.");
			return null;
		}
		
		StringBuilder stringBuilder = new StringBuilder();
		String line;
		try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {	
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line);
			}
		}

		return stringBuilder.toString();
	}

	/**
	 * Sends a POST request with default timeouts (3000ms) and retry settings.
	 * 
	 * @param postParameters The parameters to include in the POST body.
	 * @param url The URL to send the request to.
	 * @return The response body as a String.
	 * @throws Exception If the request fails.
	 */
	public static String reqPostMethod(NameValuePair[] postParameters, String url) throws Exception {
		return reqPostMethod(postParameters, url, 3000, 3000, 2, false);
	}

	/**
	 * Sends a POST request with configurable timeouts and retry settings.
	 * 
	 * @param postParameters  The parameters to include in the POST body.
	 * @param url             The URL to send the request to.
	 * @param socketTimeout   The socket timeout in milliseconds.
	 * @param connectTimeout  The connection timeout in milliseconds.
	 * @param maxRetries      The maximum number of retry attempts.
	 * @param isRetryEnabled  Whether retries are enabled.
	 * @return The response body as a String.
	 * @throws Exception If the request fails.
	 */
	public static String reqPostMethod(NameValuePair[] postParameters, String url, int socketTimeout, 
			int connectTimeout, int maxRetries, boolean isRetryEnabled) throws Exception {

		if (url == null || url.trim().isEmpty()) {
			throw new IllegalArgumentException("URL cannot be null or empty");
		}

		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		String responseString = null;

		try {
			// Configure request timeout settings
			RequestConfig config = RequestConfig.custom()
					.setConnectTimeout(connectTimeout)
					.setSocketTimeout(socketTimeout)
					.build();

			// Build the HttpClient with the configuration and retry handler
			httpClient = HttpClients.custom()
					.setDefaultRequestConfig(config)
					.setRetryHandler(new StandardHttpRequestRetryHandler(maxRetries, isRetryEnabled))
					.build();

			// Prepare the POST request
			HttpPost httpPost = new HttpPost(url);

			// Set the form parameters if provided
			if (postParameters != null) {
				List<NameValuePair> params = Arrays.asList(postParameters);
				httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
			}

			// Execute the request
			response = httpClient.execute(httpPost);
			int statusCode = response.getStatusLine().getStatusCode();

			// Log errors for non-2xx status codes (specifically 400+ and 500+)
			if (statusCode >= 400 && statusCode < 500) {
				logger.error("{} returned 4xx status code: {}", url, statusCode);
			} else if (statusCode >= 500) {
				logger.error("{} returned 5xx status code: {}", url, statusCode);
			} else {
				// Process the successful response
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					responseString = EntityUtils.toString(entity, StandardCharsets.UTF_8);
				}
			}

		} catch (ConnectTimeoutException e) {
			logger.error("{} - Connection timed out: {}", url, e.getMessage());
			throw e;
		} catch (SocketTimeoutException e) {
			logger.error("{} - Socket timed out: {}", url, e.getMessage());
			throw e;
		} catch (ConnectException e) {
			logger.error("{} - Connection failed: {}", url, e.getMessage());
			throw e;
		} catch (UnknownHostException e) {
			logger.error("{} - Unknown host: {}", url, e.getMessage());
			throw e;
		} catch (NoRouteToHostException e) {
			logger.error("{} - No route to host: {}", url, e.getMessage());
			throw e;
		} catch (NullPointerException e) {
			logger.error("{} - Null pointer exception encountered: ", url, e);
			throw e;
		} catch (Exception e) {
			logger.error("{} - General exception occurred: ", url, e);
			throw e;
		} finally {
			// Ensure resources are closed
			closeResources(response, httpClient);
		}

		return responseString;
	}

	/**
	 * Sends a POST request with a JSON body (String).
	 * 
	 * @param jsonBody The JSON string to send.
	 * @param url      The URL to send the request to.
	 * @param headers  Optional request headers.
	 * @return The response body as a byte array.
	 * @throws Exception If the request fails.
	 */
	public static byte[] reqPostInBodyStringJsonMethod(String jsonBody, String url, Header[] headers) throws Exception {
		return reqPostInBodyMethod(jsonBody, url, null, CONTENT_TYPE_JSON, ENCODING_UTF8, 5000, 5000, 2, false);
	}

	/**
	 * Sends a POST request with a JSON body (byte array).
	 * 
	 * @param jsonBody The JSON bytes to send.
	 * @param url      The URL to send the request to.
	 * @param headers  Optional request headers.
	 * @return The response body as a byte array.
	 * @throws Exception If the request fails.
	 */
	public static byte[] reqPostInBodyByteJsonMethod(byte[] jsonBody, String url, Header[] headers) throws Exception {
		return reqPostInBodyMethod(jsonBody, url, null, CONTENT_TYPE_JSON, null, 5000, 5000, 2, false);
	}

	/**
	 * Sends a POST request with a JSON body (String) and configurable timeouts.
	 * 
	 * @param jsonBody        The JSON string to send.
	 * @param url             The URL to send the request to.
	 * @param headers         Optional request headers.
	 * @param socketTimeout   The socket timeout in milliseconds.
	 * @param connectTimeout  The connection timeout in milliseconds.
	 * @return The response body as a byte array.
	 * @throws Exception If the request fails.
	 */
	public static byte[] reqPostInBodyStringJsonMethod(String jsonBody, String url, Header[] headers, 
			int socketTimeout, int connectTimeout) throws Exception {
		return reqPostInBodyMethod(jsonBody, url, null, CONTENT_TYPE_JSON, ENCODING_UTF8, 
				socketTimeout, connectTimeout, 2, false);
	}

	/**
	 * Sends a GET request and returns the response as a JSONObject.
	 * 
	 * @param getParameters  The query parameters.
	 * @param headerMap      The request headers.
	 * @param url            The URL to send the request to.
	 * @return The response parsed as a JSONObject.
	 * @throws Exception If the request fails.
	 */
	public static JSONObject reqGetInJsonSimple(NameValuePair[] getParameters, Map<String, String> headerMap, String url) throws Exception {
		return reqGetInJsonSimple(getParameters, headerMap, url, 3000, 3000, 2, false);
	}

	/**
	 * Sends a GET request with configurable settings and returns a JSONObject.
	 * 
	 * @param getParameters   The query parameters.
	 * @param headerMap       The request headers.
	 * @param url             The URL to send the request to.
	 * @param socketTimeout   The socket timeout in milliseconds.
	 * @param connectTimeout  The connection timeout in milliseconds.
	 * @param maxRetries      The maximum number of retry attempts.
	 * @param isRetryEnabled  Whether retries are enabled.
	 * @return The response parsed as a JSONObject.
	 * @throws Exception If the request fails.
	 */
	public static JSONObject reqGetInJsonSimple(NameValuePair[] getParameters, Map<String, String> headerMap, String url, 
			int socketTimeout, int connectTimeout, int maxRetries, boolean isRetryEnabled) throws Exception {

		if (url == null || url.trim().isEmpty()) {
			throw new IllegalArgumentException("URL cannot be null or empty");
		}

		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		InputStream inputStream = null;
		InputStreamReader streamReader = null;
		JSONObject jsonObject = null;

		try {
			RequestConfig config = RequestConfig.custom()
					.setConnectTimeout(connectTimeout)
					.setSocketTimeout(socketTimeout)
					.setCookieSpec(org.apache.http.client.config.CookieSpecs.STANDARD)
					.build();

			httpClient = HttpClients.custom()
					.setDefaultRequestConfig(config)
					.setRetryHandler(new StandardHttpRequestRetryHandler(maxRetries, isRetryEnabled))
					.build();

			// Build the URI with query parameters
			URIBuilder uriBuilder = new URIBuilder(url);
			if (getParameters != null) {
				for (NameValuePair pair : getParameters) {
					uriBuilder.addParameter(pair.getName(), pair.getValue());
				}
			}
			URI uri = uriBuilder.build();

			HttpGet httpGet = new HttpGet(uri);
			
			// Set default and custom headers
			httpGet.setHeader("Content-Type", CONTENT_TYPE_TEXT_JSON);
			httpGet.addHeader("Connection", "close");

			if (headerMap != null) {
				for (Map.Entry<String, String> entry : headerMap.entrySet()) {
					httpGet.addHeader(entry.getKey(), entry.getValue());
				}
			}

			response = httpClient.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
	
			if (statusCode >= 400 && statusCode < 500) {
				logger.error("{} returned 4xx status code: {}", url, statusCode);
			} else if (statusCode >= 500) {
				logger.error("{} returned 5xx status code: {}", url, statusCode);
			} else {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					String encoding = ENCODING_UTF8;
					if (entity.getContentEncoding() != null) {
						encoding = entity.getContentEncoding().getValue();
					}
					logger.info("Response encoding: {}", encoding);
					
					inputStream = entity.getContent();
					streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
				}
			}

			if (streamReader == null) {
				logger.warn("StreamReader is null, cannot parse JSON.");
				return null;
			}

			JSONParser parser = new JSONParser();
			jsonObject = (JSONObject) parser.parse(streamReader);

		} catch (NullPointerException e) {
			logger.error("{} - NullPointerException:", url, e);
			throw e;
		} catch (Exception e) {
			logger.error("{} - Exception occurred while processing JSON response.", url, e);
			throw e;
		} finally {
			// Close all streams and clients
			if (streamReader != null) streamReader.close();
			if (inputStream != null) inputStream.close();
			closeResources(response, httpClient);
		}

		return jsonObject;
	}

	/**
	 * Generic method to send a POST request with a body (String, byte[], or Serializable object).
	 * 
	 * @param body            The body content.
	 * @param url             The URL to send the request to.
	 * @param headers         Request headers.
	 * @param contentType     The Content-Type header value.
	 * @param charSet         The character set (e.g., "UTF-8").
	 * @param socketTimeout   Socket timeout in milliseconds.
	 * @param connectTimeout  Connection timeout in milliseconds.
	 * @param maxRetries      Maximum retry attempts.
	 * @param isRetryEnabled  Whether to retry on failure.
	 * @return The response body as a byte array.
	 * @throws Exception If the request fails.
	 */
	public static byte[] reqPostInBodyMethod(Object body, String url, Header[] headers, String contentType, 
			String charSet, int socketTimeout, int connectTimeout, int maxRetries, boolean isRetryEnabled) throws Exception {

		if (url == null || url.trim().isEmpty()) {
			throw new IllegalArgumentException("URL cannot be null or empty");
		}

		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		byte[] responseBytes = null;

		try {
			RequestConfig config = RequestConfig.custom()
					.setConnectTimeout(connectTimeout)
					.setSocketTimeout(socketTimeout)
					.build();

			httpClient = HttpClients.custom()
					.setDefaultRequestConfig(config)
					.setRetryHandler(new StandardHttpRequestRetryHandler(maxRetries, isRetryEnabled))
					.build();

			HttpPost httpPost = new HttpPost(url);

			if (headers != null) {
				for (Header h : headers) {
					httpPost.addHeader(h);	
				}
			}

			// Handle different body types
			if (body instanceof byte[]) {
				ByteArrayEntity entity = new ByteArrayEntity((byte[]) body, ContentType.create(contentType));
				httpPost.setEntity(entity);
		    } else if (body instanceof String) {
		    	StringEntity entity = new StringEntity((String) body, ContentType.create(contentType, charSet));
				httpPost.setEntity(entity);
		    } else {
		        final byte[] buffer = SerializationUtils.serialize((Serializable) body);
		        ByteArrayEntity entity = new ByteArrayEntity(buffer);
		        httpPost.setEntity(entity);
		    }

			response = httpClient.execute(httpPost);
			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode >= 400 && statusCode < 500) {
				logger.error("{} returned 4xx status code: {}", url, statusCode);
			} else if (statusCode >= 500) {
				logger.error("{} returned 5xx status code: {}", url, statusCode);
			} else {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					responseBytes = EntityUtils.toByteArray(entity);
				}
			}

		} catch (ConnectTimeoutException e) {
			logger.error("{} - Connection timed out: {}", url, e.getMessage());
			throw e;
		} catch (SocketTimeoutException e) {
			logger.error("{} - Socket timed out: {}", url, e.getMessage());
			throw e;
		} catch (ConnectException e) {
			logger.error("{} - Connection failed: {}", url, e.getMessage());
			throw e;
		} catch (UnknownHostException e) {
			logger.error("{} - Unknown host: {}", url, e.getMessage());
			throw e;
		} catch (NoRouteToHostException e) {
			logger.error("{} - No route to host: {}", url, e.getMessage());
			throw e;
		} catch (NullPointerException e) {
			logger.error("{} - Null pointer exception: ", url, e);
			throw e;
		} catch (Exception e) {
			logger.error("{} - Exception: ", url, e);
			throw e;
		} finally {
			closeResources(response, httpClient);
		}

		return responseBytes;
	}

	/**
	 * Sends a POST request with custom headers.
	 * 
	 * @param postParameters  The form parameters.
	 * @param requestHeaders  The headers to send.
	 * @param url             The URL to send the request to.
	 * @return The response body as a String.
	 * @throws Exception If the request fails.
	 */
	public static String reqHeaderPostMethod(NameValuePair[] postParameters, Map<String, String> requestHeaders, String url) throws Exception {
		return reqHeaderPostMethod(postParameters, requestHeaders, url, 3000, 3000);
	}

	/**
	 * Sends a POST request with custom headers and timeouts.
	 * 
	 * @param postParameters  The form parameters.
	 * @param requestHeaders  The headers to send.
	 * @param url             The URL to send the request to.
	 * @param socketTimeout   Socket timeout.
	 * @param connectTimeout  Connection timeout.
	 * @return The response body as a String.
	 * @throws Exception If the request fails.
	 */
	public static String reqHeaderPostMethod(NameValuePair[] postParameters, Map<String, String> requestHeaders, String url, 
			int socketTimeout, int connectTimeout) throws Exception {

		if (url == null || url.trim().isEmpty()) {
			throw new IllegalArgumentException("URL cannot be null or empty");
		}

		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		String responseString = null;

		try {
			RequestConfig config = RequestConfig.custom()
					.setConnectTimeout(connectTimeout)
					.setSocketTimeout(socketTimeout)
					.build();

			httpClient = HttpClients.custom()
					.setDefaultRequestConfig(config)
					.build();

			HttpPost httpPost = new HttpPost(url);
			
			// Set headers
			if (requestHeaders != null) {
				for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
					httpPost.addHeader(entry.getKey(), entry.getValue());
				}
			}
			
			// Set parameters
			if (postParameters != null) {
				List<NameValuePair> params = Arrays.asList(postParameters);
				httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
			}

			response = httpClient.execute(httpPost);
			int statusCode = response.getStatusLine().getStatusCode();
			HttpEntity entity = response.getEntity();
			
			// Read response regardless of status code to allow error handling by caller or logging
			if (entity != null) {
				responseString = EntityUtils.toString(entity, StandardCharsets.UTF_8);
			}

			if (statusCode >= 400 && statusCode < 500) {
				logger.error("{} returned 4xx status code: {}", url, statusCode);
			} else if (statusCode >= 500) {
				logger.error("{} returned 5xx status code: {}", url, statusCode);
			}

		} catch (ConnectTimeoutException e) {
			logger.error("{} - Connection timed out: {}", url, e.getMessage());
			throw e;
		} catch (SocketTimeoutException e) {
			logger.error("{} - Socket timed out: {}", url, e.getMessage());
			throw e;
		} catch (ConnectException e) {
			logger.error("{} - Connection failed: {}", url, e.getMessage());
			throw e;
		} catch (UnknownHostException e) {
			logger.error("{} - Unknown host: {}", url, e.getMessage());
			throw e;
		} catch (NoRouteToHostException e) {
			logger.error("{} - No route to host: {}", url, e.getMessage());
			throw e;
		} catch (NullPointerException e) {
			logger.error("{} - Null pointer exception: ", url, e);
			throw e;
		} catch (Exception e) {
			logger.error("{} - Exception: ", url, e);
			throw e;
		} finally {
			closeResources(response, httpClient);
		}

		return responseString;
	}

	/**
	 * Uploads a file to the specified URL using a POST request.
	 * 
	 * @param filePartName  The name of the file part in the multipart request.
	 * @param file          The file to upload.
	 * @param headerMap     Request headers.
	 * @param parameterMap  Additional text parameters.
	 * @param url           The URL to upload to.
	 * @return The response parsed as a JSONObject.
	 * @throws Exception If the request fails.
	 */
	public static JSONObject reqPostUploadFile(String filePartName, File file, Map<String, String> headerMap, Map<String, String> parameterMap, String url) throws Exception {

		if (url == null || url.trim().isEmpty()) {
			throw new IllegalArgumentException("URL cannot be null or empty");
		}

		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		InputStream inputStream = null;
		InputStreamReader streamReader = null;
		JSONObject jsonObject = null;

		try {
			httpClient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(url);

			// Build the multipart entity
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.addBinaryBody(filePartName, file);
			
			if (parameterMap != null) {
				for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
					builder.addTextBody(entry.getKey(), entry.getValue(), ContentType.TEXT_PLAIN.withCharset(ENCODING_UTF8));
				}
			}
			
			HttpEntity multipart = builder.build();
			httpPost.setEntity(multipart);

			// Set headers
			if (headerMap != null) {
				for (Map.Entry<String, String> entry : headerMap.entrySet()) {
					httpPost.addHeader(entry.getKey(), entry.getValue());
				}
			}

			response = httpClient.execute(httpPost);
			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode >= 400 && statusCode < 500) {
				logger.error("{} returned 4xx status code: {}", url, statusCode);
			} else if (statusCode >= 500) {
				logger.error("{} returned 5xx status code: {}", url, statusCode);
			} else {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					inputStream = entity.getContent();
					streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
				}
			}

			if (streamReader == null) {
				logger.warn("StreamReader is null, cannot parse JSON response.");
				return null;
			}

			JSONParser parser = new JSONParser();
			jsonObject = (JSONObject) parser.parse(streamReader);

		} catch (ConnectTimeoutException e) {
			logger.error("{} - Connection timed out: {}", url, e.getMessage());
			throw e;
		} catch (SocketTimeoutException e) {
			logger.error("{} - Socket timed out: {}", url, e.getMessage());
			throw e;
		} catch (ConnectException e) {
			logger.error("{} - Connection failed: {}", url, e.getMessage());
			throw e;
		} catch (UnknownHostException e) {
			logger.error("{} - Unknown host: {}", url, e.getMessage());
			throw e;
		} catch (NoRouteToHostException e) {
			logger.error("{} - No route to host: {}", url, e.getMessage());
			throw e;
		} catch (NullPointerException e) {
			logger.error("{} - Null pointer exception: ", url, e);
			throw e;
		} catch (Exception e) {
			logger.error("{} - Exception: ", url, e);
			throw e;
		} finally {
			if (streamReader != null) streamReader.close();
			if (inputStream != null) inputStream.close();
			closeResources(response, httpClient);
		}

		return jsonObject;
	}

	/**
	 * Downloads a file from the specified URL using a POST request.
	 * 
	 * @param postParameters  Parameters to include in the POST request.
	 * @param headerMap       Request headers.
	 * @param url             The URL to download from.
	 * @return The file content as a byte array.
	 * @throws Exception If the request fails.
	 */
	public static byte[] reqPostDownloadFile(NameValuePair[] postParameters, Map<String, String> headerMap, String url) throws Exception {

		if (url == null || url.trim().isEmpty()) {
			throw new IllegalArgumentException("URL cannot be null or empty");
		}

		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		byte[] responseBytes = null;

		try {
			RequestConfig config = RequestConfig.custom()
					.setConnectTimeout(5000)
					.setSocketTimeout(5000)
					.build();

			httpClient = HttpClients.custom()
					.setDefaultRequestConfig(config)
					.build();

			HttpPost httpPost = new HttpPost(url);
			
			// Set form parameters
			if (postParameters != null) {
				List<NameValuePair> params = Arrays.asList(postParameters);
				httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
			}

			// Set headers
			if (headerMap != null) {
				for (Map.Entry<String, String> entry : headerMap.entrySet()) {
					httpPost.addHeader(entry.getKey(), entry.getValue());
				}
			}

			response = httpClient.execute(httpPost);
			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode >= 400 && statusCode < 500) {
				logger.error("{} returned 4xx status code: {}", url, statusCode);
			} else if (statusCode >= 500) {
				logger.error("{} returned 5xx status code: {}", url, statusCode);
			} else {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					responseBytes = EntityUtils.toByteArray(entity);
				}
			}

		} catch (ConnectTimeoutException e) {
			logger.error("{} - Connection timed out: {}", url, e.getMessage());
			throw e;
		} catch (SocketTimeoutException e) {
			logger.error("{} - Socket timed out: {}", url, e.getMessage());
			throw e;
		} catch (ConnectException e) {
			logger.error("{} - Connection failed: {}", url, e.getMessage());
			throw e;
		} catch (UnknownHostException e) {
			logger.error("{} - Unknown host: {}", url, e.getMessage());
			throw e;
		} catch (NoRouteToHostException e) {
			logger.error("{} - No route to host: {}", url, e.getMessage());
			throw e;
		} catch (NullPointerException e) {
			logger.error("{} - Null pointer exception: ", url, e);
			throw e;
		} catch (Exception e) {
			logger.error("{} - Exception: ", url, e);
			throw e;
		} finally {
			closeResources(response, httpClient);
		}

		return responseBytes;
	}

	/**
	 * Sends a POST request with a JSON body and custom headers (RPC style).
	 * 
	 * @param jsonBody        The JSON string to send.
	 * @param requestHeaders  Request headers.
	 * @param url             The URL to send the request to.
	 * @return The response body as a String.
	 * @throws Exception If the request fails.
	 */
	public static String reqHeadersPostMethodRpc(String jsonBody, Map<String, String> requestHeaders, 
			String url) throws Exception {
		return reqHeadersPostMethodRpc(jsonBody, requestHeaders, url, 3000, 3000);
	}

	/**
	 * Sends a POST request with a JSON body, custom headers, and configurable timeouts.
	 * 
	 * @param jsonBody        The JSON string to send.
	 * @param requestHeaders  Request headers.
	 * @param url             The URL to send the request to.
	 * @param socketTimeout   Socket timeout.
	 * @param connectTimeout  Connection timeout.
	 * @return The response body as a String.
	 * @throws Exception If the request fails.
	 */
	public static String reqHeadersPostMethodRpc(String jsonBody, Map<String, String> requestHeaders, 
			String url, int socketTimeout, int connectTimeout) throws Exception {

		if (url == null || url.trim().isEmpty()) {
			throw new IllegalArgumentException("URL cannot be null or empty");
		}

		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		String responseString = null;

		try {
			RequestConfig config = RequestConfig.custom()
					.setConnectTimeout(connectTimeout)
					.setSocketTimeout(socketTimeout)
					.build();

			httpClient = HttpClients.custom()
					.setDefaultRequestConfig(config)
					.build();

			HttpPost httpPost = new HttpPost(url);
			
			// Set headers
			if (requestHeaders != null) {
				for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
					httpPost.addHeader(entry.getKey(), entry.getValue());
				}
			}
			
			// Set the JSON body
			StringEntity requestEntity = new StringEntity(jsonBody, ContentType.create("application/json", "utf-8"));
			httpPost.setEntity(requestEntity);

			response = httpClient.execute(httpPost);
			int statusCode = response.getStatusLine().getStatusCode();
			HttpEntity entity = response.getEntity();

			// Read response regardless of status
			if (entity != null) {
				responseString = EntityUtils.toString(entity, StandardCharsets.UTF_8);
			}

			if (statusCode >= 400 && statusCode < 500) {
				logger.error("{} returned 4xx status code: {}", url, statusCode);
			} else if (statusCode >= 500) {
				logger.error("{} returned 5xx status code: {}", url, statusCode);
			}

		} catch (ConnectTimeoutException e) {
			logger.error("{} - Connection timed out: {}", url, e.getMessage());
			throw e;
		} catch (SocketTimeoutException e) {
			logger.error("{} - Socket timed out: {}", url, e.getMessage());
			throw e;
		} catch (ConnectException e) {
			logger.error("{} - Connection failed: {}", url, e.getMessage());
			throw e;
		} catch (UnknownHostException e) {
			logger.error("{} - Unknown host: {}", url, e.getMessage());
			throw e;
		} catch (NoRouteToHostException e) {
			logger.error("{} - No route to host: {}", url, e.getMessage());
			throw e;
		} catch (NullPointerException e) {
			logger.error("{} - Null pointer exception: ", url, e);
			throw e;
		} catch (Exception e) {
			logger.error("{} - Exception: ", url, e);
			throw e;
		} finally {
			closeResources(response, httpClient);
		}

		return responseString;
	}
	
	/**
	 * Sends a GET request.
	 * 
	 * @param getParameters The query parameters.
	 * @param url           The URL to send the request to.
	 * @return The response body as a String.
	 * @throws Exception If the request fails.
	 */
	public static String reqGetMethod(NameValuePair[] getParameters, String url) throws Exception {
		return reqGetMethod(getParameters, url, 3000, 3000, 2, false);
	}

	/**
	 * Sends a GET request with configurable timeouts.
	 * 
	 * @param getParameters   The query parameters.
	 * @param url             The URL.
	 * @param socketTimeout   Socket timeout.
	 * @param connectTimeout  Connection timeout.
	 * @return The response body as a String.
	 * @throws Exception If the request fails.
	 */
	public static String reqGetMethod(NameValuePair[] getParameters, String url, int socketTimeout, 
			int connectTimeout) throws Exception {
		return reqGetMethod(getParameters, url, socketTimeout, connectTimeout, 2, false);
	}

	/**
	 * Sends a GET request with configurable timeouts and retries.
	 * 
	 * @param getParameters   The query parameters.
	 * @param url             The URL.
	 * @param socketTimeout   Socket timeout.
	 * @param connectTimeout  Connection timeout.
	 * @param maxRetries      Max retries.
	 * @param isRetryEnabled  Retry enabled.
	 * @return The response body as a String.
	 * @throws Exception If the request fails.
	 */
	public static String reqGetMethod(NameValuePair[] getParameters, String url, int socketTimeout, 
			int connectTimeout, int maxRetries, boolean isRetryEnabled) throws Exception {

		if (url == null || url.trim().isEmpty()) {
			throw new IllegalArgumentException("URL cannot be null or empty");
		}

		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		String responseString = null;

		try {
			RequestConfig config = RequestConfig.custom()
					.setConnectTimeout(connectTimeout)
					.setSocketTimeout(socketTimeout)
					.setCookieSpec(org.apache.http.client.config.CookieSpecs.STANDARD)
					.build();

			httpClient = HttpClients.custom()
					.setDefaultRequestConfig(config)
					.setRetryHandler(new StandardHttpRequestRetryHandler(maxRetries, isRetryEnabled))
					.build();

			URIBuilder uriBuilder = new URIBuilder(url);
			if (getParameters != null) {
				for (NameValuePair pair : getParameters) {
					uriBuilder.addParameter(pair.getName(), pair.getValue());
				}
			}
			URI uri = uriBuilder.build();
			
			HttpGet httpGet = new HttpGet(uri);
			httpGet.setHeader("Content-Type", CONTENT_TYPE_XML);
			httpGet.addHeader("Connection", "close");

			response = httpClient.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode >= 400 && statusCode < 500) {
				logger.error("{} returned 4xx status code: {}", url, statusCode);
			} else if (statusCode >= 500) {
				logger.error("{} returned 5xx status code: {}", url, statusCode);
			} else {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					responseString = EntityUtils.toString(entity, StandardCharsets.UTF_8);
				}
			}

		} catch (ConnectTimeoutException e) {
			logger.error("{} - Connection timed out: {}", url, e.getMessage());
			throw e;
		} catch (SocketTimeoutException e) {
			logger.error("{} - Socket timed out: {}", url, e.getMessage());
			throw e;
		} catch (ConnectException e) {
			logger.error("{} - Connection failed: {}", url, e.getMessage());
			throw e;
		} catch (UnknownHostException e) {
			logger.error("{} - Unknown host: {}", url, e.getMessage());
			throw e;
		} catch (NoRouteToHostException e) {
			logger.error("{} - No route to host: {}", url, e.getMessage());
			throw e;
		} catch (NullPointerException e) {
			logger.error("{} - Null pointer exception: ", url, e);
			throw e;
		} catch (Exception e) {
			logger.error("{} - Exception: ", url, e);
			throw e;
		} finally {
			closeResources(response, httpClient);
		}

		return responseString;
	}

	/**
	 * Sends a GET request and reads the response as a stream (converted to String).
	 * 
	 * @param getParameters The query parameters.
	 * @param url           The URL.
	 * @return The response as a String.
	 * @throws Exception If the request fails.
	 */
	public static String reqGetMethodAsStream(NameValuePair[] getParameters, String url) throws Exception {
		return reqGetMethodAsStream(getParameters, url, 3000, 3000, 2, false);
	}

	/**
	 * Sends a GET request and reads the response as a stream with configurable options.
	 * 
	 * @param getParameters   The query parameters.
	 * @param url             The URL.
	 * @param socketTimeout   Socket timeout.
	 * @param connectTimeout  Connection timeout.
	 * @param maxRetries      Max retries.
	 * @param isRetryEnabled  Retry enabled.
	 * @return The response as a String.
	 * @throws Exception If the request fails.
	 */
	public static String reqGetMethodAsStream(NameValuePair[] getParameters, String url, int socketTimeout, int connectTimeout, int maxRetries, boolean isRetryEnabled) throws Exception {

		if (url == null || url.trim().isEmpty()) {
			throw new IllegalArgumentException("URL cannot be null or empty");
		}

		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		InputStream inputStream = null;
		String responseString = null;

		try {
			RequestConfig config = RequestConfig.custom()
					.setConnectTimeout(connectTimeout)
					.setSocketTimeout(socketTimeout)
					.setCookieSpec(org.apache.http.client.config.CookieSpecs.STANDARD)
					.build();

			httpClient = HttpClients.custom()
					.setDefaultRequestConfig(config)
					.setRetryHandler(new StandardHttpRequestRetryHandler(maxRetries, isRetryEnabled))
					.build();

			URIBuilder uriBuilder = new URIBuilder(url);
			if (getParameters != null) {
				for (NameValuePair pair : getParameters) {
					uriBuilder.addParameter(pair.getName(), pair.getValue());
				}
			}
			URI uri = uriBuilder.build();

			HttpGet httpGet = new HttpGet(uri);
			httpGet.setHeader("Content-Type", CONTENT_TYPE_TEXT_JSON);
			httpGet.addHeader("Connection", "close");

			response = httpClient.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode >= 400 && statusCode < 500) {
				logger.error("{} returned 4xx status code: {}", url, statusCode);
			} else if (statusCode >= 500) {
				logger.error("{} returned 5xx status code: {}", url, statusCode);
			} else {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					inputStream = entity.getContent();
					Writer writer = new StringWriter();
					char[] buffer = new char[1024];
					try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
				        int n;
				        while((n = reader.read(buffer)) != -1) {
				        	writer.write(buffer, 0, n);
				        }
		            } finally {
		            	if (inputStream != null) inputStream.close();
		            }
			        responseString = writer.toString();
				}
			}

		} catch (Exception e) {
			logger.error("{} - Exception: ", url, e);
			throw e;
		} finally {
			closeResources(response, httpClient);
		}

		return responseString;
	}

	/**
	 * Sends a POST request and reads the response as a stream (converted to String).
	 * 
	 * @param postParameters  The form parameters.
	 * @param url             The URL.
	 * @return The response as a String.
	 * @throws Exception If the request fails.
	 */
	public static String reqPostMethodAsStream(NameValuePair[] postParameters, String url) throws Exception {
		return reqPostMethodAsStream(postParameters, url, 3000, 3000, 2, false);
	}

	/**
	 * Sends a POST request and reads the response as a stream with configurable options.
	 * 
	 * @param postParameters  The form parameters.
	 * @param url             The URL.
	 * @param socketTimeout   Socket timeout.
	 * @param connectTimeout  Connection timeout.
	 * @param maxRetries      Max retries.
	 * @param isRetryEnabled  Retry enabled.
	 * @return The response as a String.
	 * @throws Exception If the request fails.
	 */
	public static String reqPostMethodAsStream(NameValuePair[] postParameters, String url, int socketTimeout, int connectTimeout, int maxRetries, boolean isRetryEnabled) throws Exception {

		if (url == null || url.trim().isEmpty()) {
			throw new IllegalArgumentException("URL cannot be null or empty");
		}

		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		InputStream inputStream = null;
		String responseString = null;

		try {
			RequestConfig config = RequestConfig.custom()
					.setConnectTimeout(connectTimeout)
					.setSocketTimeout(socketTimeout)
					.setCookieSpec(org.apache.http.client.config.CookieSpecs.STANDARD)
					.build();

			httpClient = HttpClients.custom()
					.setDefaultRequestConfig(config)
					.setRetryHandler(new StandardHttpRequestRetryHandler(maxRetries, isRetryEnabled))
					.build();

			HttpPost httpPost = new HttpPost(url);
			httpPost.setHeader("Content-Type", CONTENT_TYPE_TEXT_JSON);
			httpPost.addHeader("Connection", "close");
			
			if (postParameters != null) {
				List<NameValuePair> params = Arrays.asList(postParameters);
				httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
			}

			response = httpClient.execute(httpPost);
			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode >= 400 && statusCode < 500) {
				logger.error("{} returned 4xx status code: {}", url, statusCode);
			} else if (statusCode >= 500) {
				logger.error("{} returned 5xx status code: {}", url, statusCode);
			} else {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					inputStream = entity.getContent();
					Writer writer = new StringWriter();
					char[] buffer = new char[1024];
					try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
				        int n;
				        while((n = reader.read(buffer)) != -1) {
				        	writer.write(buffer, 0, n);
				        }
		            } finally {
		            	if (inputStream != null) inputStream.close();
		            }
			        responseString = writer.toString();
				}
			}

		} catch (Exception e) {
			logger.error("{} - Exception: ", url, e);
			throw e;
		} finally {
			closeResources(response, httpClient);
		}

		return responseString;
	}

	/**
     * Authenticates using an OAuth Access Token.
     * 
     * @param  getParameters Parameters for the OAuth API.
     * @param  url           The OAuth API's URL.
     * @param  accessToken   The Access Token.
     * @return The response as a String.
     * @throws Exception If an execution error occurs.
     */
	public static String reqOauthGetMethod(NameValuePair[] getParameters, String url, String accessToken) throws Exception {
		return reqOauthGetMethod(getParameters, url, accessToken , 3000, 3000, 3, false);
	}

	/**
	 * Authenticates using an OAuth Access Token with configurable options.
	 * 
	 * @param getParameters   Parameters for the OAuth API.
	 * @param url             The OAuth API's URL.
	 * @param accessToken     The Access Token.
	 * @param socketTimeout   Socket timeout.
	 * @param connectTimeout  Connection timeout.
	 * @param maxRetries      Max retries.
	 * @param isRetryEnabled  Retry enabled.
	 * @return The response as a String.
	 * @throws Exception If an execution error occurs.
	 */
	public static String reqOauthGetMethod(NameValuePair[] getParameters, String url, String accessToken, 
			int socketTimeout, int connectTimeout, int maxRetries, boolean isRetryEnabled) throws Exception {

		if (url == null || url.trim().isEmpty()) {
			throw new IllegalArgumentException("URL cannot be null or empty");
		}

		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		String responseString = null;

		try {
			RequestConfig config = RequestConfig.custom()
					.setConnectTimeout(connectTimeout)
					.setSocketTimeout(socketTimeout)
					.build();

			httpClient = HttpClients.custom()
					.setDefaultRequestConfig(config)
					.setRetryHandler(new StandardHttpRequestRetryHandler(maxRetries, isRetryEnabled))
					.build();

			URIBuilder uriBuilder = new URIBuilder(url);
			if (getParameters != null) {
				for (NameValuePair pair : getParameters) {
					uriBuilder.addParameter(pair.getName(), pair.getValue());
				}
			}
			URI uri = uriBuilder.build();

			HttpGet httpGet = new HttpGet(uri);
			httpGet.setHeader("Authorization", accessToken);

			response = httpClient.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode >= 400 && statusCode < 500) {
				logger.error("{} returned 4xx status code: {}", url, statusCode);
				throw new Exception("HTTP 4xx Error: " + statusCode);
			} else if (statusCode >= 500) {
				logger.error("{} returned 5xx status code: {}", url, statusCode);
				throw new Exception("HTTP 5xx Error: " + statusCode);
			} else {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					responseString = EntityUtils.toString(entity, StandardCharsets.UTF_8);
				}
			}

		} catch (ConnectTimeoutException e) {
			logger.error("{} - Connection timed out: {}", url, e.getMessage());
			throw e;
		} catch (SocketTimeoutException e) {
			logger.error("{} - Socket timed out: {}", url, e.getMessage());
			throw e;
		} catch (ConnectException e) {
			logger.error("{} - Connection failed: {}", url, e.getMessage());
			throw e;
		} catch (UnknownHostException e) {
			logger.error("{} - Unknown host: {}", url, e.getMessage());
			throw e;
		} catch (NoRouteToHostException e) {
			logger.error("{} - No route to host: {}", url, e.getMessage());
			throw e;
		} catch (NullPointerException e) {
			logger.error("{} - Null pointer exception: ", url, e);
			throw e;
		} finally {
			closeResources(response, httpClient);
		}

		return responseString;
	}

	/**
	 * Helper method to safely close HTTP resources.
	 * 
	 * @param response   The HTTP response to close.
	 * @param httpClient The HTTP client to close.
	 */
	private static void closeResources(CloseableHttpResponse response, CloseableHttpClient httpClient) {
		if (response != null) {
			try {
				response.close();
			} catch (IOException e) {
				logger.error("Error closing response", e);
			}
		}
		if (httpClient != null) {
			try {
				httpClient.close();
			} catch (IOException e) {
				logger.error("Error closing client", e);
			}
		}
	}
	
}
