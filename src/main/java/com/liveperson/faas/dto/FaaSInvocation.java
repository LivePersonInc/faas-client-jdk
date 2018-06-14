package com.liveperson.faas.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object of the invocation
 *
 * @author arotaru
 */
public class FaaSInvocation<T> {
    private long timestamp;
    private List<FaaSKeyValue> headers;
    private T payload;

    public FaaSInvocation() {
        this.timestamp = System.currentTimeMillis();
        this.headers = new ArrayList<FaaSKeyValue>();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<FaaSKeyValue> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headerMap) {
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            this.headers.add(new FaaSKeyValue(entry.getKey(), entry.getValue()));
        }
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }
}
