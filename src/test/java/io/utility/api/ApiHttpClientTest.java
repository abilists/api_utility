package io.utility.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class ApiHttpClientTest {

    private MockWebServer server;

    @Before
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @After
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    public void testReqPostMethod() throws Exception {
        // Enqueue response
        server.enqueue(new MockResponse().setBody("echoResponse"));

        NameValuePair[] params = { new BasicNameValuePair("param1", "value1") };
        String fullUrl = server.url("/echo").toString();
        
        String response = ApiHttpClient.reqPostMethod(params, fullUrl);
        
        assertEquals("echoResponse", response);
        
        RecordedRequest request = server.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/echo", request.getPath());
    }

    @Test
    public void testReqGetInJsonSimple() throws Exception {
        server.enqueue(new MockResponse()
            .setBody("{\"key\":\"value\"}")
            .addHeader("Content-Type", "application/json"));

        Map<String, String> headers = new HashMap<>();
        headers.put("X-Custom-Header", "test");
        
        String fullUrl = server.url("/json").toString();
        JSONObject json = ApiHttpClient.reqGetInJsonSimple(null, headers, fullUrl);
        
        assertNotNull(json);
        assertEquals("value", json.get("key"));
        
        RecordedRequest request = server.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("test", request.getHeader("X-Custom-Header"));
    }

    @Test
    public void testTimeout() {
        server.enqueue(new MockResponse()
            .setBody("timeout")
            .setBodyDelay(2000, TimeUnit.MILLISECONDS)); // Delay 2s

        try {
            // Set timeout to 100ms
            String fullUrl = server.url("/timeout").toString();
            ApiHttpClient.reqGetMethod(null, fullUrl, 100, 100);
            fail("Expected SocketTimeoutException");
        } catch (SocketTimeoutException e) {
            // Success
        } catch (Exception e) {
            fail("Expected SocketTimeoutException but got " + e.getClass().getName());
        }
    }
    
    @Test
    public void test404() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(404).setBody("Not Found"));
        
        String fullUrl = server.url("/error").toString();
        String response = ApiHttpClient.reqGetMethod(null, fullUrl);
        
        // According to current logic, it logs error but returns body if entity exists
        assertEquals("Not Found", response);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNullUrl() throws Exception {
        ApiHttpClient.reqPostMethod(null, null);
    }
}
