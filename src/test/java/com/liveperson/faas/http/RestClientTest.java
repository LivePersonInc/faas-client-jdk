package com.liveperson.faas.http;

import com.liveperson.faas.exception.RestException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RestClientTest {

    private static int mockServerPort = 9000;
    private static HttpServer mockServer;
    private static String baseUrl = "http://localhost:" + mockServerPort;

    @BeforeClass
    public static void beforeClass() throws Exception {
        mockServer = HttpServer.create(new InetSocketAddress(mockServerPort), 0);
        mockServer.createContext("/successHandler", new RestClientTest.SuccessHandler());
        mockServer.createContext("/errorHandler", new RestClientTest.ErrorHandler());
        mockServer.createContext("/returnRequestBodyHandler", new RestClientTest.ReturnRequestBodyHandler());
        mockServer.setExecutor(null);
        mockServer.start();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        mockServer.stop(0);
    }

    @Test
    public void successPostResponseTest() throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("test", "test");
        RestClient restClient = new DefaultRestClient();
        String response = restClient.post(baseUrl + "/successHandler", headers, "test", 15000);
        assertEquals("Response not correctly received", "Test", response);
    }

    @Test
    public void successPostResponseTestWithDefaultTimeOut() throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("test", "test");
        RestClient restClient = new DefaultRestClient();
        String response = restClient.post(baseUrl + "/successHandler", headers, "test");
        assertEquals("Response not correctly received", "Test", response);
    }


    @Test
    public void successPostResponseRightEncodingTest() throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        RestClient restClient = new DefaultRestClient();
        String jsonBody = "{\"encoding\":\"Englishåøñ \uD83D\uDD96 \uD83D\uDE02 ಕನ್ನಡ\"}";
        String response = restClient.post(baseUrl + "/returnRequestBodyHandler", headers, jsonBody, 15000);
        assertEquals("Response not correctly received", jsonBody, response);
    }

    @Test
    public void errorPostResponseTest() throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("test", "test");

        RestClient restClient = new DefaultRestClient();
        try {
            String response = restClient.post(baseUrl + "/errorHandler", headers, "test", 15000);
        } catch (RestException e) {
            assertEquals(e.getResponse(), "Error");
        }
    }

    @Test
    public void errorPostResponseTestWithDefaultTimeOut() throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("test", "test");

        RestClient restClient = new DefaultRestClient();
        try {
            String response = restClient.post(baseUrl + "/errorHandler", headers, "test");
        } catch (RestException e) {
            assertEquals(e.getResponse(), "Error");
        }
    }

    @Test
    public void successGetResponseTest() throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("test", "test");

        RestClient restClient = new DefaultRestClient();
        String response = restClient.get(baseUrl + "/successHandler", headers, 15000);
        assertEquals("Response not correctly received", "Test", response);
    }

    @Test
    public void successGetResponseTestWithDefaultTimeOut() throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("test", "test");

        RestClient restClient = new DefaultRestClient();
        String response = restClient.get(baseUrl + "/successHandler", headers);
        assertEquals("Response not correctly received", "Test", response);
    }

    @Test(expected = IOException.class)
    public void errorGetResponseTest() throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("test", "test");

        RestClient restClient = new DefaultRestClient();
        restClient.get(baseUrl + "/errorHandler", headers, 15000);
    }

    @Test(expected = IOException.class)
    public void errorGetResponseTestWithDefaultTimeOut() throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("test", "test");

        RestClient restClient = new DefaultRestClient();
        restClient.get(baseUrl + "/errorHandler", headers);
    }

    //Define handler for success route of http server
    public static class SuccessHandler implements HttpHandler {

        public void handle(HttpExchange he) throws IOException {
            String response = "Test";
            he.sendResponseHeaders(202, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    //Define handler for error route of http server
    public static class ErrorHandler implements HttpHandler {

        public void handle(HttpExchange he) throws IOException {
            String response = "Error";
            he.sendResponseHeaders(400, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    //Define handler for success route of http server with same response body as request body
    public static class ReturnRequestBodyHandler implements HttpHandler {

        public void handle(HttpExchange he) throws IOException {
            //Read request body
            InputStreamReader input = new InputStreamReader( he.getRequestBody());
            int i;
            String output = "";
            while ((i = input.read()) != -1){
                output += (char)i;
            }
            input.close();

            String response = output;

            //Encode request body and send it as response body
            String encoding = "UTF-8";
            he.getResponseHeaders().set("Content-Type", "text/html; charset=" + encoding);

            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            he.sendResponseHeaders(202, bytes.length);

            OutputStream os = he.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }
}
