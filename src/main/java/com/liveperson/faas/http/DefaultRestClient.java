package com.liveperson.faas.http;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class DefaultRestClient implements RestClient{

    private int readTimeout;
    private int connectTimeout;

    /**
     * Constructor
     * @param readTimeout set the timeout for retrieving data
     * @param connectTimeout set the timeout for the socket connection
     */
    public DefaultRestClient(int readTimeout, int connectTimeout){
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    /**
     * Set the headers to the connection object
     * @param connection HttpURLConnection instance
     * @param headers headers map
     */
    private static void setHeaders(HttpURLConnection connection, Map<String, String> headers){
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(),entry.getValue());
        }
    }

    /**
     * Set the body to the connection object
     * @param connection HttpURLConnection instance
     * @param jsonBody json string
     * @throws IOException
     */
    private static void sendBody(HttpURLConnection connection, String jsonBody) throws IOException {
        OutputStream outputStream = connection.getOutputStream();
        DataOutputStream output = new DataOutputStream(outputStream);

        try {
            output.writeBytes(jsonBody);
            output.flush();
            output.close();
        } catch(IOException e){
            output.close();
            throw e;
        }
    }

    /**
     * Read the http response body
     * @param connection HttpURLConnection instance
     * @return json string
     * @throws IOException
     */
    private static String readResponseBody(HttpURLConnection connection) throws IOException {
        BufferedReader input = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String inputLine;

        try {
            StringBuffer content = new StringBuffer();
            while ((inputLine = input.readLine()) != null) {
                content.append(inputLine);
            }
            input.close();
            return content.toString();
        } catch(IOException e){
            input.close();
            throw e;
        }
    }

    public String post(String url, Map<String, String> headers, String jsonBody) throws IOException{
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        try {
            connection.setRequestMethod(HttpMethod.POST.name());
            connection.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
            this.setHeaders(connection, headers);


            connection.setConnectTimeout(this.connectTimeout);
            connection.setReadTimeout(this.readTimeout);
            connection.setDoOutput(true);
            connection.setDoInput(true);

            this.sendBody(connection, jsonBody);

            int status = connection.getResponseCode();

            String response = "";

            if (status >= HttpStatus.BAD_REQUEST.value()) {
                connection.disconnect();
                throw new IOException("Received response code " + status + " for url " + url);
            }

            response = this.readResponseBody(connection);

            connection.disconnect();
            return response;
        } catch(Exception e){
            connection.disconnect();
            throw e;
        }
    }
}
