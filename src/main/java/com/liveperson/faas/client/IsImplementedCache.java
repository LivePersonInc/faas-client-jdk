package com.liveperson.faas.client;

import com.liveperson.faas.client.types.FaaSEventImplementedExpiry;

public interface IsImplementedCache {

    /**
     * Updates the cache by adding the value of isImplemented with eventId as its key to the cache
     *
     * @param eventId       name of the event whose isImplemented value is cached
     * @param isImplemented boolean indicating whether lambdas exist that implement the given event
     */
    void update(String eventId, boolean isImplemented);

    /**
     * Returns the cached event and its implementation value if stored and valid, otherwise should trigger removal
     * and return null
     *
     * @param eventId name of the event that is looked for
     * @return FaasEventImplementedExpiry that contains the event name and its isImplemented value or null if not found
     */
    FaaSEventImplementedExpiry getIfCachedAndValid(String eventId);
}
