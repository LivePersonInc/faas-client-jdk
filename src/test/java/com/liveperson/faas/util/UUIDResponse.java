package com.liveperson.faas.util;

/**
 * Response test data structure for invocation by lambda UUID
 */
public class UUIDResponse {
    public String key;
    public String value;

    @Override
    public String toString() {
        return String.format("{key: %s, value: %s}", key, value);
    }
}
