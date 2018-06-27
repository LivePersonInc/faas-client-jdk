package com.liveperson.faas.http;

import java.io.IOException;
import java.util.Map;

/**
 * Rest client for communicating with the Eventsource-Gateway (a.k.a Asgard) RESTful API
 *
 * @author arotaru
 */
public interface RestClient {

    /**
     * Execute a http post call
     * @param url the url to the API endpoint
     * @param headers the headers values map
     * @param jsonBody the json body as string
     * @return response body as string
     * @throws IOException
     */
    public String post(String url, Map<String, String> headers, String jsonBody) throws IOException;
}
