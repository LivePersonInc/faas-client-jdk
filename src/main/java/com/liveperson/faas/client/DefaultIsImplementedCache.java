package com.liveperson.faas.client;

import com.liveperson.faas.client.types.FaaSEventImplementedExpiry;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultIsImplementedCache implements IsImplementedCache {

    private int isImplementedCacheDurationInSeconds;
    private ConcurrentMap<String, FaaSEventImplementedExpiry> implementationCache = new ConcurrentHashMap<>();

    DefaultIsImplementedCache(int isImplementedCacheDurationInSeconds) {
        this.isImplementedCacheDurationInSeconds = isImplementedCacheDurationInSeconds;
    }

    public void update(String eventId, boolean isImplemented) {
        FaaSEventImplementedExpiry faaSEventImplementedExpiry = new FaaSEventImplementedExpiry();
        faaSEventImplementedExpiry.setExpirationDate(LocalDateTime.now().plusSeconds(isImplementedCacheDurationInSeconds));
        faaSEventImplementedExpiry.setImplemented(isImplemented);
        implementationCache.put(eventId, faaSEventImplementedExpiry);
    }

    private boolean hasBeenCachedAndIsValid(String eventId) {
        return implementationCache.containsKey(eventId) && implementationCache.get(eventId).getExpirationDate().isAfter(LocalDateTime.now());
    }

    private void removeIfExpired(String eventId) {
        if (implementationCache.containsKey(eventId) && implementationCache.get(eventId).getExpirationDate().isBefore(LocalDateTime.now())) {
            implementationCache.remove(eventId);
        }
    }

    public FaaSEventImplementedExpiry getIfCachedAndValid(String eventId) {
        if (hasBeenCachedAndIsValid(eventId)) {
            return this.implementationCache.get(eventId);
        }
        removeIfExpired(eventId);
        return null;

    }

}
