package com.liveperson.faas.client;

import com.liveperson.faas.client.types.OptionalParams;
import com.liveperson.faas.csds.CsdsMapClient;
import com.liveperson.faas.dto.FaaSInvocation;
import com.liveperson.faas.exception.*;
import com.liveperson.faas.http.DefaultRestClient;
import com.liveperson.faas.metriccollector.MetricCollector;
import com.liveperson.faas.response.lambda.ErrorLogResponseObject;
import com.liveperson.faas.response.lambda.LambdaResponse;
import com.liveperson.faas.security.AuthSignatureBuilder;
import com.liveperson.faas.util.AuthBearerGenerator;
import com.liveperson.faas.util.BearerConfigResponseObject;
import com.liveperson.faas.util.EventResponse;
import com.liveperson.faas.util.UUIDResponse;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FaaSIntegrationClientTest {
    private static AuthSignature authSignatureBuilder;
    private FaaSWebClient client;
    private FaaSWebClient clientWithBearer;
    private DefaultRestClient restClient = new DefaultRestClient();
    private MetricCollector metricCollector;
    private DefaultIsImplementedCache defaultIsImplementedCache;
    private String accountId = System.getenv("ACCOUNT_ID");
    private String clientId = System.getenv("CLIENT_ID");
    private String clientSecret = System.getenv("CLIENT_SECRET");
    private String userId;
    private String externalSystem = "test_system";
    private FaaSEvent event = FaaSEvent.MessagingNewConversation;
    private String lambdaUUID = System.getenv("LAMBDAUUID");
    private String requestId = "requestId";

    private int defaultTimeOut = 15000;

    private OptionalParams optionalParams;

    @Before
    public void before() throws Exception, TokenGenerationException {
        client = getFaaSClient();
        authSignatureBuilder = new AuthSignature();
        clientWithBearer = getFaaSClientBearer();
        optionalParams = new OptionalParams();
        optionalParams.setTimeOutInMs(defaultTimeOut);
        optionalParams.setRequestId(requestId);
        userId = authSignatureBuilder.getUserId();
    }

    @Test
    public void getLambdas() throws Exception {
        List<LambdaResponse> lambdaResponse = clientWithBearer.getLambdas(userId, new HashMap<String, String>(),
                optionalParams);
        assertTrue(lambdaResponse.size() > 0);
        assertTrue(lambdaResponse.toString().contains("name"));
        assertTrue(lambdaResponse.toString().contains("uuid"));
        assertTrue(lambdaResponse.toString().contains("createdAt"));
        assertTrue(lambdaResponse.toString().contains("updatedAt"));
        assertTrue(lambdaResponse.toString().contains("updatedBy"));
    }

    @Test
    public void getLambdasWithOptionalQueryParameters() throws Exception {
        HashMap<String, String> filterMap = new HashMap<String, String>();
        filterMap.put("eventId", "not-existing");
        List<LambdaResponse> lambdaResponse = clientWithBearer.getLambdas(userId, filterMap, optionalParams);
        assertTrue(lambdaResponse.size() == 0);
    }

    @Test(expected = FaaSException.class)
    public void getLambdasWithInvalidStateValueQueryParameter() throws IOException, FaaSException {
        try {
            HashMap<String, String> filterMap = new HashMap<String, String>();
            filterMap.put("state", "active");
            clientWithBearer.getLambdas(userId, filterMap, optionalParams);

        } catch (FaaSException e) {
            assertTrue(e.getCause().toString().contains("Received response code 400"));
            throw e;
        }
    }

    @Test
    public void isImplementedEventRetrievedFromCache() throws Exception {
        boolean isImplemented = client.isImplemented(externalSystem, event, optionalParams);
        assertTrue("Lambda should be implemented", isImplemented);
    }

    @Test
    public void isImplementedEventRetrievedFromCacheOnNotExistingEvent() throws Exception {
        boolean isImplemented = client.isImplemented(externalSystem, FaaSEvent.ChatPostSurveyEmailTranscript,
                optionalParams);
        assertFalse("Lambda should not be implemented", isImplemented);
    }

    @Test
    public void invokeViaEventType() throws Exception {
        UUIDResponse eventPayload = new UUIDResponse();
        long timestamp = System.currentTimeMillis();
        Map<String, String> headers = getTestHeaders();
        FaaSInvocation<UUIDResponse> invocationData = getUUIDResponseFaaSInvocation(eventPayload, timestamp, headers);
        EventResponse[] response = client.invokeByEvent(externalSystem, event, invocationData, EventResponse[].class,
                optionalParams);
        assertEquals("Success", response[0].result.value);
        assertNotNull(response[0].uuid, "The uuid should not be null");
    }

    @Test
    public void invokeViaEventTypeWithValidPayload() throws Exception {
        UUIDResponse eventPayload = new UUIDResponse();
        eventPayload.value = "validLogs";
        long timestamp = System.currentTimeMillis();
        Map<String, String> headers = getTestHeaders();
        FaaSInvocation<UUIDResponse> invocationData = getUUIDResponseFaaSInvocation(eventPayload, timestamp, headers);
        EventResponse[] response = client.invokeByEvent(externalSystem, event, invocationData, EventResponse[].class,
                optionalParams);
        assertEquals("With Payload", response[0].result.value);
    }

    @Test
    public void invokeViaEventTypeWithNonExistingEvent() throws Exception {
        UUIDResponse eventPayload = new UUIDResponse();
        long timestamp = System.currentTimeMillis();
        Map<String, String> headers = getTestHeaders();
        FaaSInvocation<UUIDResponse> invocationData = getUUIDResponseFaaSInvocation(eventPayload, timestamp, headers);
        EventResponse[] response = client.invokeByEvent(externalSystem, FaaSEvent.ChatPostSurveyEmailTranscript,
                invocationData, EventResponse[].class, optionalParams);
        assertTrue(response.length == 0);
    }

    @Test
    public void invokeViaUUIDWithRequestId() throws Exception {
        String payload = "request_data";
        long timestamp = System.currentTimeMillis();
        FaaSInvocation<String> invocationData = new FaaSInvocation<String>(null, payload);
        invocationData.setTimestamp(timestamp);
        optionalParams.setRequestId(requestId);

        String response = client.invokeByUUID(externalSystem, lambdaUUID, invocationData, String.class, optionalParams);
        assertEquals("Success", response);
    }

    @Test
    public void invokeViaUUIDWithoutPayload() throws Exception {
        long timestamp = System.currentTimeMillis();
        FaaSInvocation<Object> invocationData = new FaaSInvocation<Object>(null, null);
        invocationData.setTimestamp(timestamp);
        String response = client.invokeByUUID(externalSystem, lambdaUUID, invocationData, String.class, optionalParams);
        assertEquals("Success", response);
    }

    @Test
    public void invokeViaUUIDWithValidPayload() throws Exception {
        String payload = "validLogs";
        long timestamp = System.currentTimeMillis();
        FaaSInvocation<Object> invocationData = new FaaSInvocation<Object>(null, payload);
        invocationData.setTimestamp(timestamp);
        String response = client.invokeByUUID(externalSystem, lambdaUUID, invocationData, String.class, optionalParams);
        assertEquals("validLogs", response);
    }

    @Test(expected = FaaSException.class)
    public void invokeViaUUIDWithTimeoutPayload() throws IOException, FaaSException {

        String payload = "timeout";
        long timestamp = System.currentTimeMillis();
        FaaSInvocation<String> invocationData = new FaaSInvocation<String>(null, payload);
        invocationData.setTimestamp(timestamp);
        optionalParams.setRequestId(requestId);
        try {
            client.invokeByUUID(externalSystem, lambdaUUID, invocationData, ErrorLogResponseObject.class,
                    optionalParams);
        } catch (FaaSException e) {
            assertEquals("Error occured during lambda invocation", e.getMessage());
            assertEquals("Read timed out", e.getCause().getMessage());
            throw e;
        }
    }

    @Test(expected = FaaSDetailedException.class)
    public void invokeViaUUIDThrowsFaasDetailedException() throws FaaSException, FaaSDetailedException {
        
        String payload = "error";
        long timestamp = System.currentTimeMillis();
        FaaSInvocation<Object> invocationData = new FaaSInvocation<Object>(null, payload);
        invocationData.setTimestamp(timestamp);
        try {
            client.invokeByUUID(externalSystem, lambdaUUID, invocationData, optionalParams);
        } catch (FaaSDetailedException e) {
            assertEquals(FaaSLambdaErrorCodesV1.RUNTIME_EXCEPTION.getCode(), e.getFaaSError().getErrorCode());
            assertEquals(901, e.getCause().getStatusCode());
            throw e;
        }
    }

    private FaaSWebClient getFaaSClient() {
        return new FaaSWebClient.Builder(accountId).withClientId(clientId)
                .withClientSecret(clientSecret)
                .withRestClient(restClient)
                .withMetricCollector(metricCollector)
                .withIsImplementedCache(defaultIsImplementedCache)
                .build();
    }

    private FaaSWebClient getFaaSClientBearer() {
        return new FaaSWebClient.Builder(accountId).withAuthSignatureBuilder(authSignatureBuilder)
                .withRestClient(restClient)
                .withMetricCollector(metricCollector)
                .withIsImplementedCache(defaultIsImplementedCache)
                .build();
    }

    private Map<String, String> getTestHeaders() {
        Map<String, String> headers = new HashMap();
        headers.put("testHeader", "testHeaderValue");
        return headers;
    }

    private FaaSInvocation<UUIDResponse> getUUIDResponseFaaSInvocation(UUIDResponse payload, long timestamp,
            Map<String, String> headers) {
        FaaSInvocation<UUIDResponse> invocationData = new FaaSInvocation();
        invocationData.setHeaders(headers);
        invocationData.setPayload(payload);
        invocationData.setTimestamp(timestamp);
        return invocationData;
    }

}

class AuthSignature implements AuthSignatureBuilder {

    private DefaultRestClient restClient = new DefaultRestClient();
    private CsdsMapClient csdsClient = new CsdsMapClient(getAlphaDomains());
    private String accountId = System.getenv("ACCOUNT_ID");
    private String username = System.getenv("USER");
    private String password = System.getenv("PASS");
    private AuthBearerGenerator bearerGenerator;
    private BearerConfigResponseObject configData;
    private String authHeader;

    public AuthSignature() throws TokenGenerationException {
        this.bearerGenerator = new AuthBearerGenerator(restClient, csdsClient, accountId, username, password);
        this.authHeader = bearerGenerator.retrieveBearerToken();
        this.configData = bearerGenerator.retrieveBearerConfig();
    }

    @Override
    public String getAuthHeader() {
        return authHeader;
    }

    public String getUserId() {
        return configData.getUserId();
    }

    private Map<String, String> getAlphaDomains() {
        Map<String, String> domains = new HashMap();
        domains.put("agentVep", "va-a.agentvep.liveperson.net");
        return domains;
    }
}