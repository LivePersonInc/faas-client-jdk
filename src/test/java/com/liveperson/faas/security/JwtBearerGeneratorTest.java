package com.liveperson.faas.security;

import com.liveperson.faas.csds.CsdsClient;
import com.liveperson.faas.exception.TokenGenerationException;
import com.liveperson.faas.http.RestClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.FieldSetter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class JwtBearerGeneratorTest {

    private static final String CLIENT_SECRET = "secret";
    private static final String ACCOUNT_ID = "accountId";
    private static final String SERVICE_NAME = "sentinel";
    private static final String CLIENT_ID = "clientId";
    private static final String SENTINEL_BASE_URL = "sentinel.com";
    private static final String JWT_URL = "https://" + SENTINEL_BASE_URL + String.format("/sentinel/api/account/%s" +
            "/app/token?v=2" +
            ".0&grant_type=client_credentials&client_id=%s&client_secret=%s", ACCOUNT_ID, CLIENT_ID, CLIENT_SECRET);
    private static final String RESPONSE = "{\"access_token\":\"Valid\",\"token_type\":\"Bearer\"}";
    private static final Map<String, String> HEADERS = new HashMap<>();
    private static final String VALID_JWT = "Valid";
    private static final String EXPIRED_JWT = "Expired";

    @Mock
    private RestClient restClient;

    @Mock
    private CsdsClient csdsClient;

    @Mock
    private AuthExpiryTester authExpiryTester;

    @InjectMocks
    private BearerGenerator bearerGenerator = new JwtBearerGenerator(restClient, csdsClient, ACCOUNT_ID,
            CLIENT_ID,
            CLIENT_SECRET);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        HEADERS.put("Content-type", "application/x-www-form-urlencoded");

    }

    @Test
    public void retrieveJwtInitializeJwtSucceeds() throws Exception {
        when(csdsClient.getDomain(SERVICE_NAME)).thenReturn(SENTINEL_BASE_URL);
        when(restClient.post(JWT_URL, HEADERS, "")).thenReturn(RESPONSE);

        assertEquals(bearerGenerator.retrieveBearerToken(), "Bearer " + VALID_JWT);
    }

    @Test(expected = TokenGenerationException.class)
    public void retrieveJwtInitializeJwtFails() throws Exception {
        when(csdsClient.getDomain(SERVICE_NAME)).thenReturn(SENTINEL_BASE_URL);
        when(restClient.post(JWT_URL, HEADERS, "")).thenThrow(new IOException("Test Exception"));

        bearerGenerator.retrieveBearerToken();
    }

    @Test
    public void requestJwtAboutToExpire() throws Exception {
        String jwtAboutToExpire = "aboutToExpire";
        // Set private member variable
        new FieldSetter(bearerGenerator, JwtBearerGenerator.class.getDeclaredField("currentJwt")).set(jwtAboutToExpire);

        when(authExpiryTester.isAboutToExpire(jwtAboutToExpire)).thenReturn(true);
        when(csdsClient.getDomain(SERVICE_NAME)).thenReturn(SENTINEL_BASE_URL);
        when(restClient.post(JWT_URL, HEADERS, "")).thenReturn(RESPONSE);

        assertEquals(bearerGenerator.retrieveBearerToken(), "Bearer " + VALID_JWT);

    }

    @Test
    public void requestJwtNotAboutToExpireAndNotNull() throws Exception {
        // Set private member variable
        new FieldSetter(bearerGenerator, JwtBearerGenerator.class.getDeclaredField("currentJwt")).set(VALID_JWT);

        when(authExpiryTester.isAboutToExpire(VALID_JWT)).thenReturn(false);
        when(csdsClient.getDomain(SERVICE_NAME)).thenReturn(SENTINEL_BASE_URL);
        when(restClient.post(JWT_URL, HEADERS, "")).thenReturn(RESPONSE);
        when(authExpiryTester.isExpired(VALID_JWT)).thenReturn(false);

        assertEquals(bearerGenerator.retrieveBearerToken(), "Bearer " + VALID_JWT);

    }

    @Test(expected = TokenGenerationException.class)
    public void requestJwtCanNotCreateNewAndCurrentExpired() throws Exception {
        // Set private member variable
        new FieldSetter(bearerGenerator, JwtBearerGenerator.class.getDeclaredField("currentJwt")).set(EXPIRED_JWT);

        when(authExpiryTester.isAboutToExpire(EXPIRED_JWT)).thenReturn(true);
        when(csdsClient.getDomain(SERVICE_NAME)).thenReturn(SENTINEL_BASE_URL);
        when(restClient.post(JWT_URL, HEADERS, "")).thenThrow(new IOException("Test Exception"));
        when(authExpiryTester.isExpired(EXPIRED_JWT)).thenReturn(true);

        bearerGenerator.retrieveBearerToken();
    }

}
