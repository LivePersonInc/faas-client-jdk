package com.liveperson.faas.client;

import com.liveperson.faas.client.types.FaaSEventImplementedExpiry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DefaultIsImplementedCacheTest {
    private static final String EVENT = "event";

    @Spy
    private IsImplementedCache isImplementedCache = new DefaultIsImplementedCache(200);

    @Test
    public void update() {
        isImplementedCache.update(EVENT, true);

        verify(isImplementedCache, times(1)).update(EVENT, true);
    }

    @Test
    public void getIfCachedAndValidSuccess() throws NoSuchFieldException {
        FaaSEventImplementedExpiry faaSEventImplementedExpiry = new FaaSEventImplementedExpiry();
        faaSEventImplementedExpiry.setExpirationDate(LocalDateTime.now().plusHours(3));
        faaSEventImplementedExpiry.setImplemented(true);
        ConcurrentMap<String, FaaSEventImplementedExpiry> implementationCache = new ConcurrentHashMap<>();
        implementationCache.putIfAbsent(EVENT, faaSEventImplementedExpiry);
        // set private field in isImplementedCache
        new FieldSetter(isImplementedCache, DefaultIsImplementedCache.class.getDeclaredField(
                "implementationCache")).set(implementationCache);

        FaaSEventImplementedExpiry faaSEventImplementedExpiry1 = isImplementedCache.getIfCachedAndValid(EVENT);

        assertEquals(faaSEventImplementedExpiry, faaSEventImplementedExpiry1);
    }

    @Test
    public void getIfCachedAndValidFailure() throws NoSuchFieldException {
        FaaSEventImplementedExpiry faaSEventImplementedExpiry = new FaaSEventImplementedExpiry();
        faaSEventImplementedExpiry.setExpirationDate(LocalDateTime.now().minusHours(2));
        faaSEventImplementedExpiry.setImplemented(true);
        ConcurrentMap<String, FaaSEventImplementedExpiry> implementationCache = new ConcurrentHashMap<>();
        implementationCache.putIfAbsent(EVENT, faaSEventImplementedExpiry);
        // set private field in isImplementedCache
        new FieldSetter(isImplementedCache, DefaultIsImplementedCache.class.getDeclaredField(
                "implementationCache")).set(implementationCache);

        assertNull(isImplementedCache.getIfCachedAndValid(EVENT));
    }

}
