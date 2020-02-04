package com.liveperson.faas.dto;

import com.fasterxml.jackson.databind.ObjectMapper;

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

    public FaaSInvocation(Map<String, String> headerMap, T payload) {
        this.timestamp = System.currentTimeMillis();
        this.headers = new ArrayList<FaaSKeyValue>();
        setHeaders(headerMap);
        this.payload = payload;
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
        if (headerMap == null)
            return;
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

    @Override
    public String toString(){
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            String json = objectMapper.writeValueAsString(this);
            return json.replaceAll("\"payload\":null}$", "\"payload\":{}}");
        } catch(Exception e){
            return "";
        }
    }
}
