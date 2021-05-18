package com.liveperson.faas.http;

import com.liveperson.faas.exception.RestException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class DefaultRestClient implements RestClient {

    private int defaultTimeOut = 15000;

    /**
     * Constructor
     */
    public DefaultRestClient() {

    }

    /**
     * Set the headers to the connection object
     *
     * @param connection HttpURLConnection instance
     * @param headers    headers map
     */
    private static void setHeaders(HttpURLConnection connection, Map<String, String> headers) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Set the body to the connection object
     *
     * @param connection HttpURLConnection instance
     * @param jsonBody   json string
     * @throws IOException
     */
    private static void sendBody(HttpURLConnection connection, String jsonBody) throws IOException {
        OutputStream outputStream = connection.getOutputStream();
        OutputStreamWriter output = new OutputStreamWriter(outputStream, "UTF-8");
        try {
            output.write(jsonBody);
            output.flush();
            output.close();
        } catch (IOException e) {
            output.close();
            throw e;
        }
    }

    /**
     * Read the http response body
     *
     * @param inputStream InputStream instance
     * @return json string
     * @throws IOException
     */
    private static String readResponseBody(InputStream inputStream) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
        String inputLine;

        try {
            StringBuffer content = new StringBuffer();
            while ((inputLine = input.readLine()) != null) {
                content.append(inputLine);
            }
            input.close();
            return content.toString();
        } catch (IOException e) {
            input.close();
            throw e;
        }
    }

    public String post(String url, Map<String, String> headers, String jsonBody) throws IOException {
        return postRequest(url, headers, jsonBody, defaultTimeOut);
    }

    public String post(String url, Map<String, String> headers, String jsonBody, int timeOutInMs) throws IOException {
        return postRequest(url, headers, jsonBody, timeOutInMs);
    }

    public String get(String url, Map<String, String> headers, int timeOutInMs) throws IOException {
        return getResponse(url, headers, timeOutInMs);
    }

    public String get(String url, Map<String, String> headers) throws IOException {
        return getResponse(url, headers, defaultTimeOut);
    }

    private String postRequest(String url, Map<String, String> headers, String jsonBody, int timeOutInMs) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        try {
            connection.setRequestMethod(HttpMethod.POST.name());
            connection.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
            this.setHeaders(connection, headers);

            connection.setConnectTimeout((int) (timeOutInMs * 0.25));
            connection.setReadTimeout((int) (timeOutInMs * 0.75));
            connection.setDoOutput(true);
            connection.setDoInput(true);

            this.sendBody(connection, jsonBody);

            int status = connection.getResponseCode();

            String response = "";

            if (status >= HttpStatus.BAD_REQUEST.value()) {
                response = this.readResponseBody(connection.getErrorStream());
                connection.disconnect();
                throw new RestException("Received response code " + status + " for url " + url, response, status);
            }

            response = this.readResponseBody(connection.getInputStream());

            connection.disconnect();
            return response;
        } catch (Exception e) {
            connection.disconnect();
            throw e;
        }
    }

    private String getResponse(String url, Map<String, String> headers, int timeOutInMs) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        try {
            connection.setRequestMethod(HttpMethod.GET.name());
            this.setHeaders(connection, headers);

            connection.setConnectTimeout((int) (timeOutInMs * 0.25));
            connection.setReadTimeout((int) (timeOutInMs * 0.75));
            connection.setDoOutput(true);
            connection.setDoInput(true);

            int status = connection.getResponseCode();

            String response = "";

            if (status >= HttpStatus.BAD_REQUEST.value()) {
                connection.disconnect();
                throw new IOException("Received response code " + status + " for url " + url);
            }

            response = this.readResponseBody(connection.getInputStream());

            connection.disconnect();
            return response;
        } catch (Exception e) {
            connection.disconnect();
            throw e;
        }
    }
}
