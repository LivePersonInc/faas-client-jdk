package com.liveperson.faas.client.types;

import java.util.UUID;

public class OptionalParams {

    private String requestId = "";
    private int timeOutInMs = 15000;

    public String getRequestId() {
        if (requestId.equals("")) {
            return UUID.randomUUID().toString();
        }
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public int getTimeOutInMs() {
        return timeOutInMs;
    }

    public void setTimeOutInMs(int timeOutInMs) {
        this.timeOutInMs = timeOutInMs;
    }
}
