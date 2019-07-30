package com.liveperson.faas.dto;

/**
 * Data Transfer Object for key/value mapping
 *
 * @author arotaru
 */
public class FaaSKeyValue {
    private String key;
    private String value;

    public FaaSKeyValue(String key, String value){
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
