package com.liveperson.faas.csds;


import com.liveperson.faas.exception.CsdsRetrievalException;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CsdsMapClientTest {

    private static final String SERVICE = "service";
    private static final String BASE_URI = "uri";
    private Map<String, String> serviceMap;
    private CsdsClient csdsClient;

    @Before
    public void setUp() {
        serviceMap = Collections.singletonMap(SERVICE, BASE_URI);
        csdsClient = new CsdsMapClient(serviceMap);
    }

    @Test
    public void getDomainSuccess() throws CsdsRetrievalException {
        assertEquals(csdsClient.getDomain(SERVICE), BASE_URI);
    }

    @Test(expected = CsdsRetrievalException.class)
    public void getDomainCsdsEntryNotFound() throws CsdsRetrievalException {
        assertEquals(csdsClient.getDomain("Does not Exist"), BASE_URI);
    }


}