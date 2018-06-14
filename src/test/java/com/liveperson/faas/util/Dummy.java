package com.liveperson.faas.util;

public class Dummy {
    public String key;
    public String value;

    @Override
    public String toString() {
        return String.format("{key: %s, value: %s}", key, value);
    }
}
