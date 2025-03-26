package com.liveperson.faas.systemtest;

import com.liveperson.faas.client.DefaultIsImplementedCache;
import com.liveperson.faas.client.FaaSEvent;
import com.liveperson.faas.client.FaaSLambdaErrorCodesV1;
import com.liveperson.faas.client.FaaSWebClient;
import com.liveperson.faas.client.types.OptionalParams;
import com.liveperson.faas.csds.CsdsMapClient;
import com.liveperson.faas.dto.FaaSInvocation;
import com.liveperson.faas.exception.*;
import com.liveperson.faas.http.DefaultRestClient;
import com.liveperson.faas.metriccollector.MetricCollector;
import com.liveperson.faas.response.lambda.LambdaResponse;
import com.liveperson.faas.security.AuthSignatureBuilder;
import com.liveperson.faas.util.AuthBearerGenerator;
import com.liveperson.faas.util.BearerConfigResponseObject;
import com.liveperson.faas.util.EventResponse;
import com.liveperson.faas.util.UUIDResponse;

import io.github.cdimascio.dotenv.Dotenv;

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

public class FaaSIntegrationClientTestV1 {
    private static AuthSignatureV1 authSignatureBuilder;
    private FaaSWebClient client;
    private FaaSWebClient clientWithBearer;
    private DefaultRestClient restClient = new DefaultRestClient();
    private MetricCollector metricCollector;
    private DefaultIsImplementedCache defaultIsImplementedCache;
    private String accountId = System.getenv("ACCOUNT_ID_V1");
    private String clientId = System.getenv("CLIENT_ID_V1");
    private String clientSecret = System.getenv("CLIENT_SECRET_V1");
    private String lambdaUUID = System.getenv("LAMBDA_UUID_V1");
    private String userId;
    private String externalSystem = "test_system";
    private FaaSEvent event = FaaSEvent.ConversationalCommand;
    private String requestId = "requestId";

    private int defaultTimeOut = 15000;

    private OptionalParams optionalParams;

    @Before
    public void before() throws Exception, TokenGenerationException {
        String env = System.getenv("ENV");
        if (env != "prod") {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            accountId = dotenv.get("ACCOUNT_ID_V1");
            clientId = dotenv.get("CLIENT_ID_V1");
            clientSecret = dotenv.get("CLIENT_SECRET_V1");
            lambdaUUID = dotenv.get("LAMBDA_UUID_V1");
        }
        client = getFaaSClient();
        authSignatureBuilder = new AuthSignatureV1();
        clientWithBearer = getFaaSClientBearer();
        optionalParams = new OptionalParams();
        optionalParams.setTimeOutInMs(defaultTimeOut);
        optionalParams.setRequestId(requestId);
        userId = authSignatureBuilder.getUserId();
    }

    @Test
    public void isV2Domain() throws Exception {
        Boolean isV2 = clientWithBearer.isV2Domain();
        assertFalse(isV2);
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
        Map<String, String> headers = getTestHeaders("success");
        FaaSInvocation<UUIDResponse> invocationData = getUUIDResponseFaaSInvocation(eventPayload, timestamp, headers);
        EventResponse[] response = client.invokeByEvent(externalSystem, event, invocationData, EventResponse[].class,
                optionalParams);
        assertEquals("success", response[0].result.value);
        assertNotNull(response[0].uuid, "The uuid should not be null");
    }

    @Test
    public void invokeViaEventTypeWithValidPayload() throws Exception {
        UUIDResponse eventPayload = new UUIDResponse();
        eventPayload.value = "validLogs";
        long timestamp = System.currentTimeMillis();
        Map<String, String> headers = getTestHeaders("success");
        FaaSInvocation<UUIDResponse> invocationData = getUUIDResponseFaaSInvocation(eventPayload, timestamp, headers);
        EventResponse[] response = client.invokeByEvent(externalSystem, event, invocationData, EventResponse[].class,
                optionalParams);
        assertEquals("success", response[0].result.value);
    }

    @Test
    public void invokeViaEventTypeWithNonExistingEvent() throws Exception {
        UUIDResponse eventPayload = new UUIDResponse();
        long timestamp = System.currentTimeMillis();
        Map<String, String> headers = getTestHeaders("success");
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
        assertEquals("success", response);
    }

    @Test
    public void invokeViaUUIDWithoutPayload() throws Exception {
        long timestamp = System.currentTimeMillis();
        FaaSInvocation<Object> invocationData = new FaaSInvocation<Object>(null, null);
        invocationData.setTimestamp(timestamp);
        String response = client.invokeByUUID(externalSystem, lambdaUUID, invocationData, String.class, optionalParams);
        assertEquals("success", response);
    }

    @Test
    public void invokeViaUUIDWithValidPayload() throws Exception {
        String payload = "validLogs";
        long timestamp = System.currentTimeMillis();
        FaaSInvocation<Object> invocationData = new FaaSInvocation<Object>(null, payload);
        invocationData.setTimestamp(timestamp);
        String response = client.invokeByUUID(externalSystem, lambdaUUID, invocationData, String.class, optionalParams);
        assertEquals("success", response);
    }

    @Test(expected = FaaSDetailedExceptionV1.class)
    public void invokeViaUUIDThrowsFaasDetailedException() throws FaaSException, FaaSDetailedException {
        long timestamp = System.currentTimeMillis();
        Map<String, String> headers = getTestHeaders("error");
        FaaSInvocation<Object> invocationData = new FaaSInvocation<Object>(headers, "error");
        invocationData.setTimestamp(timestamp);
        try {
            client.invokeByUUID(externalSystem, lambdaUUID, invocationData, optionalParams);
        } catch (FaaSDetailedExceptionV1 e) {
            assertEquals(FaaSLambdaErrorCodesV1.CUSTOM_FAILURE.getCode(), e.getFaaSError().getErrorCode());
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

    private Map<String, String> getTestHeaders(String run) {
        Map<String, String> headers = new HashMap();
        headers.put("run", run);
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

class AuthSignatureV1 implements AuthSignatureBuilder {

    private DefaultRestClient restClient = new DefaultRestClient();
    private CsdsMapClient csdsClient = new CsdsMapClient(getAlphaDomains());
    private String accountId = System.getenv("ACCOUNT_ID_V1");
    private String username = System.getenv("USER_NAME");
    private String password = System.getenv("PASSWORD");
    private AuthBearerGenerator bearerGenerator;
    private BearerConfigResponseObject configData;
    private String authHeader;

    public AuthSignatureV1() throws TokenGenerationException {
        String env = System.getenv("ENV");
        if (env != "prod") {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            accountId = dotenv.get("ACCOUNT_ID_V1");
            username = dotenv.get("USER_NAME");
            password = dotenv.get("PASSWORD");
        }

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