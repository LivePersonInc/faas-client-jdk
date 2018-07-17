package com.liveperson.faas.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class RestClientTest {

    private static int mockServerPort = 9000;
    private static HttpServer mockServer;
    private static String baseUrl = "http://localhost:" + mockServerPort;

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

    @BeforeClass
    public static void beforeClass() throws Exception{
        mockServer = HttpServer.create(new InetSocketAddress(mockServerPort), 0);
        mockServer.createContext("/successHandler", new RestClientTest.SuccessHandler());
        mockServer.createContext("/errorHandler", new RestClientTest.ErrorHandler());
        mockServer.setExecutor(null);
        mockServer.start();
    }

    @AfterClass
    public static void afterClass() throws Exception{
        mockServer.stop(0);
    }

    @Test
    public void successResponseTest() throws Exception{
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("test", "test");

        RestClient restClient = new DefaultRestClient(3000, 3000);
        String response = restClient.post( baseUrl + "/successHandler", headers, "test");
        assertEquals("Response not correctly received", "Test", response);
    }

    @Test(expected = IOException.class)
    public void errorResponseTest() throws Exception{
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("test", "test");

        RestClient restClient = new DefaultRestClient(3000, 3000);
        String response = restClient.post(baseUrl + "/errorHandler", headers, "test");
    }
}
