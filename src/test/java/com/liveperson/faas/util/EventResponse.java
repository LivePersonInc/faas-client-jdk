package com.liveperson.faas.util;

import java.util.Date;

public class EventResponse {
    public String uuid;
    public Date timestamp;
    public UUIDResponse result;

    @Override
    public String toString() {
        return String.format("{uuid: %s, timestamp: %s, result: %s}", uuid, timestamp, result);
    }
}
