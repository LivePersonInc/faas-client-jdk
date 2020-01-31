package com.liveperson.faas.csds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liveperson.faas.csds.types.BaseURI;
import com.liveperson.faas.csds.types.BaseURIs;
import com.liveperson.faas.exception.CsdsRetrievalException;
import com.liveperson.faas.http.RestClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.FieldSetter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class CsdsWebClientTest {

    private static final String ACCOUNT_ID = "account";
    private static final String SERVICE = "service";
    private static final String BASE_URI = "baseURI";
    private BaseURI baseURI;
    private List<BaseURI> baseURIList;
    @Mock
    private ObjectMapper mapper;
    @Mock
    private RestClient restClient;
    @InjectMocks
    private CsdsWebClient client = new CsdsWebClient(restClient, ACCOUNT_ID);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        BaseURI baseURI = new BaseURI();
        baseURI.setAccount(ACCOUNT_ID);
        baseURI.setBaseURI(BASE_URI);
        baseURI.setService(SERVICE);
        this.baseURI = baseURI;
        baseURIList = Collections.singletonList(baseURI);

    }

    @Test
    public void getDomainSuccessDomainsAlreadyInitialized() throws NoSuchFieldException, CsdsRetrievalException {
        BaseURI baseURI = new BaseURI();
        baseURI.setAccount(ACCOUNT_ID);
        baseURI.setBaseURI(BASE_URI);
        baseURI.setService(SERVICE);
        // Set private member variables
        new FieldSetter(client, CsdsWebClient.class.getDeclaredField("cachedDomains")).set(baseURIList);
        new FieldSetter(client, CsdsWebClient.class.getDeclaredField("cacheExpiryDate")).set(LocalDateTime.now().plusHours(2));

        assertEquals(client.getDomain(SERVICE), BASE_URI);
    }

    @Test(expected = CsdsRetrievalException.class)
    public void getDomainCsdsEntryNotFound() throws NoSuchFieldException, CsdsRetrievalException {
        List<BaseURI> baseURIs = new ArrayList<BaseURI>(Collections.singletonList(baseURI));
        // Set private member variables
        new FieldSetter(client, CsdsWebClient.class.getDeclaredField("cachedDomains")).set(baseURIs);
        new FieldSetter(client, CsdsWebClient.class.getDeclaredField("cacheExpiryDate")).set(LocalDateTime.now().plusHours(2));

        client.getDomain("doesNotExist");
    }

    @Test
    public void getDomainSuccessDomainsNotYetInitalized() throws IOException, CsdsRetrievalException {
        String csdsRetrivalUrl = String.format("http://%s/api/account/%s/service/baseURI.json?version=1.0",
                "api.liveperson.net", ACCOUNT_ID);
        String response = "response";
        BaseURIs baseURIs = new BaseURIs();
        baseURIs.setBaseURIS(baseURIList);
        when(restClient.get(eq(csdsRetrivalUrl), any())).thenReturn(response);
        when(mapper.readValue(response, BaseURIs.class)).thenReturn(baseURIs);

        assertEquals(client.getDomain(SERVICE), BASE_URI);
    }

    @Test(expected = CsdsRetrievalException.class)
    public void getDomainsFailureToRetrieveFromServer() throws IOException, CsdsRetrievalException {
        when(restClient.get(any(), any())).thenThrow(new IOException());

        client.getDomain(SERVICE);
    }
}