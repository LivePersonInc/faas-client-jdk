package com.liveperson.faas.exception;

import java.io.IOException;

public class RestException extends IOException {
    private String response;
    private int statusCode;

    public RestException(String message, String response, int statusCode, Throwable cause){
        super(message, cause);

        this.response = response;
        this.statusCode = statusCode;
    }

    public RestException(String message, String response, int statusCode){
        super(message);

        this.response = response;
        this.statusCode = statusCode;
    }

    public String getResponse() {
        return response;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
