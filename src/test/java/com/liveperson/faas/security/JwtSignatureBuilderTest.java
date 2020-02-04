package com.liveperson.faas.security;

import com.liveperson.faas.csds.CsdsClient;
import com.liveperson.faas.exception.TokenGenerationException;
import com.liveperson.faas.http.RestClient;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class JwtSignatureBuilderTest {

    private static final String CLIENT_SECRET = "secret";
    private static final String ACCOUNT_ID = "accountId";
    private static final String CLIENT_ID = "clientId";

    @Mock
    private RestClient restClient;

    @Mock
    private CsdsClient csdsClient;

    @Mock
    private BearerGenerator bearerGenerator;

    @Mock
    private Logger logger;

    @InjectMocks
    private AuthSignatureBuilder jwtSignatureBuilder = new JwtSignatureBuilder(restClient, csdsClient,
            ACCOUNT_ID, CLIENT_ID, CLIENT_SECRET);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getAuthHeaderSuccess() throws TokenGenerationException {
        String bearerToken = "Bearer";

        when(bearerGenerator.retrieveBearerToken()).thenReturn(bearerToken);

        assertEquals(bearerToken, jwtSignatureBuilder.getAuthHeader());

    }

    @Test(expected = TokenGenerationException.class)
    public void getAuthHeaderExceptionThrown() throws TokenGenerationException {
        when(bearerGenerator.retrieveBearerToken()).thenThrow(new TokenGenerationException("Failure"));

        jwtSignatureBuilder.getAuthHeader();
    }
}
