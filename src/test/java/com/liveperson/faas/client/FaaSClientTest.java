package com.liveperson.faas.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liveperson.faas.client.types.FaaSEventImplementedExpiry;
import com.liveperson.faas.client.types.OptionalParams;
import com.liveperson.faas.csds.CsdsClient;
import com.liveperson.faas.dto.FaaSError;
import com.liveperson.faas.dto.FaaSInvocation;
import com.liveperson.faas.exception.*;
import com.liveperson.faas.http.RestClient;
import com.liveperson.faas.metriccollector.MetricCollector;
import com.liveperson.faas.response.lambda.LambdaResponse;
import com.liveperson.faas.security.AuthDPoPSignatureBuilder;
import com.liveperson.faas.security.AuthSignatureBuilder;
import com.liveperson.faas.util.EventResponse;
import com.liveperson.faas.util.UUIDResponse;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyFloat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Functions V2 tests
 */
@RunWith(MockitoJUnitRunner.class)
public class FaaSClientTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SimpleDateFormat mockDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    @InjectMocks
    private FaaSWebClient client;
    @InjectMocks
    private FaaSWebClient clientWithDPoP;
    @Mock
    private RestClient restClientMock;
    @Mock
    private CsdsClient csdsClientMock;
    @Captor
    private ArgumentCaptor<Map<String, String>> httpHeaderCaptor;
    @Captor
    private ArgumentCaptor<String> urlCaptor;
    @Captor
    private ArgumentCaptor<String> httpBodyCaptor;
    private String authHeader = "Bearer authenticate";
    @Mock
    private AuthSignatureBuilder authSignatureBuilder;
    @Mock
    private AuthDPoPSignatureBuilder authDPoPSignatureBuilder;
    @Mock
    private MetricCollector metricCollectorMock;
    @Mock
    private DefaultIsImplementedCache defaultIsImplementedCacheMock;
    private String accountId = "11111111";
    private String apiVersion = "1";
    private String lpEventSource = "test_system";
    private String userId = "0051393312";
    private FaaSEvent event = FaaSEvent.ChatPostSurveyEmailTranscript;
    private String lambdaUUID = "81ec57ed-b353-4c71-8543-423364db169d";
    private String faasGWUrl = "faasGW.fninvocations.com";
    private String faasUIUrl = "faasUI.functions.com";
    private String requestId = "requestId";
    private int defaultTimeOut = 15000;
    // Oauth2 + DPOP
    private String accessToken = "some_access_token";
    private String dpopHeader = "dpopJWT";

    private OptionalParams optionalParams;

    @Before
    public void before() throws Exception {
        client = getFaaSClient();
        clientWithDPoP = getFaaSClientWithDpopAuth();
        optionalParams = new OptionalParams();
        mockDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        optionalParams.setTimeOutInMs(defaultTimeOut);
        optionalParams.setRequestId(requestId);
        when(csdsClientMock.getDomain(eq(FaaSWebClient.CSDS_GW_SERVICE_NAME))).thenReturn(faasGWUrl);
        when(csdsClientMock.getDomain(eq(FaaSWebClient.CSDS_UI_SERVICE_NAME))).thenReturn(faasUIUrl);
        when(authSignatureBuilder.getAuthHeader()).thenReturn(authHeader);
        // Oauth2 + DPoP
        when(authDPoPSignatureBuilder.getAccessTokenInternal(anyString())).thenReturn(accessToken);
        when(authDPoPSignatureBuilder.getDpopHeaderInternal(anyString(), anyString(), anyString()))
                .thenReturn(dpopHeader);
    }

    @Test
    public void invokeViaUUIDWithRequestId() throws Exception {
        String payload = "request_data";
        long timestamp = System.currentTimeMillis();
        FaaSInvocation<String> invocationData = new FaaSInvocation<String>(null, payload);
        invocationData.setTimestamp(timestamp);
        optionalParams.setRequestId(requestId);

        when(restClientMock.post(eq(getExpectedInvokeUUIDUrl()), httpHeaderCaptor.capture(),
                httpBodyCaptor.capture(), eq(defaultTimeOut))).thenReturn("\"lambda_result\"");
        String response = client.invokeByUUID(lpEventSource, lambdaUUID, invocationData, String.class,
                optionalParams);

        verify(metricCollectorMock, times(1)).onInvokeByUUIDSuccess(eq(lpEventSource), anyFloat(),
                eq(lambdaUUID),
                eq(accountId));
        assertEquals("Lambda invocation with the wrong body",
                getExpectedRequestBody(timestamp, "[]", "\"request_data\""), httpBodyCaptor.getValue());
        assertTrue("Lambda invocation with wrong authorization header", httpHeaderCaptor.getValue().get(
                "Authorization").contains("Bearer"));
        assertTrue(httpHeaderCaptor.getValue().get(
                "X-REQUEST-ID").contains(requestId));
        assertTrue(httpHeaderCaptor.getValue().get(
                "LP-EventSource").contains(lpEventSource));
        assertEquals("Lambda invocation result does not match expected value",
                "lambda_result", response);
    }

    @Test
    public void invokeViaUUIDWithDPoP() throws Exception {
        String payload = "request_data";
        long timestamp = System.currentTimeMillis();
        FaaSInvocation<String> invocationData = new FaaSInvocation<String>(null, payload);
        invocationData.setTimestamp(timestamp);

        when(restClientMock.post(eq(getExpectedInvokeUUIDUrl()), httpHeaderCaptor.capture(),
                httpBodyCaptor.capture(), eq(defaultTimeOut))).thenReturn("\"lambda_result\"");
        String response = clientWithDPoP.invokeByUUID(lpEventSource, lambdaUUID, invocationData, String.class,
                optionalParams);

        verify(metricCollectorMock, times(1)).onInvokeByUUIDSuccess(eq(lpEventSource), anyFloat(),
                eq(lambdaUUID),
                eq(accountId));
        verify(authDPoPSignatureBuilder, times(1)).getAccessTokenInternal(eq("https://" + faasGWUrl));
        verify(authDPoPSignatureBuilder, times(1)).getDpopHeaderInternal(
                eq(getExpectedInvokeUUIDUrl()),
                eq("POST"), eq(accessToken));
        assertEquals("Lambda invocation with the wrong body",
                getExpectedRequestBody(timestamp, "[]", "\"request_data\""), httpBodyCaptor.getValue());
        assertTrue("Lambda invocation with wrong authorization header",
                httpHeaderCaptor.getValue().get("Authorization").equals("DPoP " + accessToken));
        assertTrue(httpHeaderCaptor.getValue().get(
                "LP-EventSource").contains(lpEventSource));
        assertTrue("Lambda invocation with wrong DPoP header",
                httpHeaderCaptor.getValue().get("DPoP").equals(dpopHeader));
        assertEquals("Lambda invocation result does not match expected value",
                "lambda_result", response);
    }

    @Test
    public void invokeViaUUIDWithStringPayload() throws Exception {
        String payload = "request_data";
        long timestamp = System.currentTimeMillis();
        FaaSInvocation<String> invocationData = new FaaSInvocation<String>(null, payload);
        invocationData.setTimestamp(timestamp);

        when(restClientMock.post(eq(getExpectedInvokeUUIDUrl()), httpHeaderCaptor.capture(),
                httpBodyCaptor.capture(), eq(defaultTimeOut))).thenReturn("\"lambda_result\"");
        String response = client.invokeByUUID(lpEventSource, lambdaUUID, invocationData, String.class,
                optionalParams);

        verify(metricCollectorMock, times(1)).onInvokeByUUIDSuccess(eq(lpEventSource), anyFloat(),
                eq(lambdaUUID),
                eq(accountId));
        assertEquals("Lambda invocation with the wrong body",
                getExpectedRequestBody(timestamp, "[]", "\"request_data\""), httpBodyCaptor.getValue());
        assertTrue(httpHeaderCaptor.getValue().get(
                "LP-EventSource").contains(lpEventSource));
        assertTrue("Lambda invocation with wrong authorization header", httpHeaderCaptor.getValue().get(
                "Authorization").contains("Bearer"));
        assertEquals("Lambda invocation result does not match expected value",
                "lambda_result", response);
    }

    @Test
    public void invokeViaUUIDWithoutPayload() throws Exception {
        long timestamp = System.currentTimeMillis();
        FaaSInvocation<Object> invocationData = new FaaSInvocation<Object>(null, null);
        invocationData.setTimestamp(timestamp);

        when(restClientMock.post(eq(getExpectedInvokeUUIDUrl()), httpHeaderCaptor.capture(),
                httpBodyCaptor.capture(), eq(defaultTimeOut))).thenReturn("\"lambda_result\"");
        String response = client.invokeByUUID(lpEventSource, lambdaUUID, invocationData, String.class,
                optionalParams);

        verify(metricCollectorMock, times(1)).onInvokeByUUIDSuccess(eq(lpEventSource), anyFloat(),
                eq(lambdaUUID),
                eq(accountId));

        assertEquals("Lambda invocation with the wrong body",
                getExpectedRequestBody(timestamp, "[]", "{}"), httpBodyCaptor.getValue());
        assertTrue("Lambda invocation with wrong authorization header", httpHeaderCaptor.getValue().get(
                "Authorization").contains("Bearer"));
        assertTrue(httpHeaderCaptor.getValue().get(
                "LP-EventSource").contains(lpEventSource));
        assertEquals("Lambda invocation result does not match expected value",
                "lambda_result", response);
    }

    @Test
    public void invokeViaUUIDWithUUIDResponsePayload() throws Exception {
        UUIDResponse payload = new UUIDResponse();
        payload.key = "requestKey";
        payload.value = "requestValue";
        long timestamp = System.currentTimeMillis();
        Map<String, String> headers = getTestHeaders();
        FaaSInvocation<UUIDResponse> invocationData = getUUIDResponseFaaSInvocation(payload, timestamp,
                headers);
        UUIDResponse expectedResponse = new UUIDResponse();
        expectedResponse.key = "responseKey";
        expectedResponse.value = "responseValue";

        when(restClientMock.post(eq(getExpectedInvokeUUIDUrl()), httpHeaderCaptor.capture(),
                httpBodyCaptor.capture(), eq(defaultTimeOut))).thenReturn("{\"key\":\"responseKey\"," +
                        "\"value\":\"responseValue\"}");
        UUIDResponse response = client.invokeByUUID(lpEventSource, lambdaUUID, invocationData,
                UUIDResponse.class,
                optionalParams);

        verify(metricCollectorMock, times(1)).onInvokeByUUIDSuccess(eq(lpEventSource), anyFloat(),
                eq(lambdaUUID),
                eq(accountId));
        assertEquals("Lambda invocation with the wrong body",
                getExpectedRequestBody(timestamp,
                        "[{\"key\":\"testHeader\",\"value\":\"testHeaderValue\"}]", "{\"key" +
                                "\":\"requestKey\",\"value\":\"requestValue\"}"),
                httpBodyCaptor.getValue());
        assertTrue("Lambda invocation with wrong authorization header", httpHeaderCaptor.getValue().get(
                "Authorization").contains("Bearer"));
        assertTrue(httpHeaderCaptor.getValue().get(
                "LP-EventSource").contains(lpEventSource));
        assertEquals("Lambda invocation result does not match expected value", expectedResponse.toString(),
                response.toString());
    }

    @Test(expected = FaaSDetailedException.class)
    public void invokeViaUUIDThrowsFaasDetailedException() throws IOException, FaaSException {
        try {
            FaaSError faaSError = new FaaSError("faas.error.code", "My custom error.");
            RestException exception = new RestException("Error during rest call.",
                    objectMapper.writeValueAsString(faaSError),
                    500);
            long timestamp = System.currentTimeMillis();
            FaaSInvocation<String> invocationData = new FaaSInvocation<String>(null, null);
            invocationData.setTimestamp(timestamp);

            when(restClientMock.post(eq(getExpectedInvokeUUIDUrl()), httpHeaderCaptor.capture(),
                    httpBodyCaptor.capture(), eq(defaultTimeOut)))
                    .thenThrow(exception);
            client.invokeByUUID(lpEventSource, lambdaUUID, invocationData, String.class, optionalParams);
        } catch (Exception ex) {
            verify(metricCollectorMock, times(1)).onInvokeByUUIDFailure(eq(lpEventSource), anyFloat(),
                    eq(lambdaUUID),
                    eq(accountId), eq(500), any());
            throw ex;
        }
    }

    @Test(expected = FaaSLambdaException.class)
    public void invokeViaUUIDThrowFaaSLambdaException() throws IOException, FaaSException {
        try {
            long timestamp = System.currentTimeMillis();
            FaaSInvocation<String> invocationData = new FaaSInvocation<String>(null, null);
            invocationData.setTimestamp(timestamp);
            FaaSError faaSError = new FaaSError(FaaSLambdaErrorCodesV1.CUSTOM_FAILURE.getCode(),
                    "My custom error.");

            when(restClientMock.post(eq(getExpectedInvokeUUIDUrl()), httpHeaderCaptor.capture(),
                    httpBodyCaptor.capture(), eq(defaultTimeOut)))
                    .thenThrow(new RestException("Error during rest call.",
                            objectMapper.writeValueAsString(faaSError),
                            500));
            client.invokeByUUID(lpEventSource, lambdaUUID, invocationData, String.class, optionalParams);
        } catch (Exception ex) {
            verify(metricCollectorMock, times(1)).onInvokeByUUIDFailure(eq(lpEventSource), anyFloat(),
                    eq(lambdaUUID),
                    eq(accountId), eq(500), any());
            throw ex;
        }
    }

    @Test(expected = FaaSException.class)
    public void invokeViaUUIDThrowsFaaSException() throws IOException, FaaSException {
        try {
            long timestamp = System.currentTimeMillis();
            FaaSInvocation<String> invocationData = new FaaSInvocation<String>(null, null);
            invocationData.setTimestamp(timestamp);
            when(restClientMock.post(eq(getExpectedInvokeUUIDUrl()), httpHeaderCaptor.capture(),
                    httpBodyCaptor.capture(), eq(defaultTimeOut)))
                    .thenThrow(NullPointerException.class);
            client.invokeByUUID(lpEventSource, lambdaUUID, invocationData, String.class, optionalParams);
        } catch (Exception ex) {
            verify(metricCollectorMock, times(1)).onInvokeByUUIDFailure(eq(lpEventSource), anyFloat(),
                    eq(lambdaUUID),
                    eq(accountId), eq(-1), any());
            throw ex;

        }
    }

    @Test
    public void invokeViaUUIDWithNoResponse() throws Exception {
        String payload = "request_data";
        long timestamp = System.currentTimeMillis();
        FaaSInvocation<String> invocationData = new FaaSInvocation<String>(null, payload);
        invocationData.setTimestamp(timestamp);
        optionalParams.setRequestId(requestId);

        when(restClientMock.post(eq(getExpectedInvokeUUIDUrl()), httpHeaderCaptor.capture(),
                httpBodyCaptor.capture(), eq(defaultTimeOut))).thenReturn("\"lambda_result\"");

        client.invokeByUUID(lpEventSource, lambdaUUID, invocationData, optionalParams);

        verify(metricCollectorMock, times(1)).onInvokeByUUIDSuccess(eq(lpEventSource), anyFloat(),
                eq(lambdaUUID),
                eq(accountId));
        assertEquals("Lambda invocation with the wrong body",
                getExpectedRequestBody(timestamp, "[]", "\"request_data\""), httpBodyCaptor.getValue());
        assertTrue("Lambda invocation with wrong authorization header", httpHeaderCaptor.getValue().get(
                "Authorization").contains("Bearer"));
        assertTrue(httpHeaderCaptor.getValue().get(
                "X-REQUEST-ID").contains(requestId));
        assertTrue(httpHeaderCaptor.getValue().get(
                "LP-EventSource").contains(lpEventSource));
    }

    private FaaSWebClient getFaaSClient() {
        return new FaaSWebClient.Builder(accountId).withCsdsClient(csdsClientMock)
                .withRestClient(restClientMock)
                .withAuthSignatureBuilder(authSignatureBuilder)
                .withMetricCollector(metricCollectorMock)
                .withIsImplementedCache(defaultIsImplementedCacheMock)
                .build();
    }

    private FaaSWebClient getFaaSClientWithDpopAuth() {
        return new FaaSWebClient.Builder(accountId).withCsdsClient(csdsClientMock)
                .withRestClient(restClientMock)
                .withAuthDPoPSignatureBuilder(this.authDPoPSignatureBuilder)
                .withMetricCollector(metricCollectorMock)
                .withIsImplementedCache(defaultIsImplementedCacheMock)
                .build();
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", authHeader);
        headers.put("X-REQUEST-ID", requestId);
        return headers;
    }

    private Map<String, String> getCompleteHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", authHeader);
        headers.put("X-REQUEST-ID", requestId);
        return headers;
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

    private FaaSInvocation<String> getStringFaaSInvocation(Map<String, String> headers) {
        FaaSInvocation<String> faaSInvocation = new FaaSInvocation<String>();
        faaSInvocation.setTimestamp(100);
        faaSInvocation.setHeaders(headers);
        faaSInvocation.setPayload("payload");
        return faaSInvocation;
    }

    private String getExpectedInvokeUUIDUrl() {
        String expectedUrl = "https://%s/api/account/%s/lambdas/%s/invoke";
        return String.format(expectedUrl, faasGWUrl, accountId, lambdaUUID);
    }

    private String getExpectedInvokeEventUrl() {
        String expectedUrl = "https://%s/api/account/%s/events/%s/invoke";
        return String.format(expectedUrl, faasGWUrl, accountId, event);
    }

    private String getExpectedIsImplementedUrl() {
        String expectedUrl = "https://%s/api/account/%s/events/%s/isImplemented";
        return String.format(expectedUrl, faasGWUrl, accountId, event);
    }

    private String getExpectedLambdasOfAnAccountUrl() {
        String expectedUrl = "https://%s/api/account/%s/lambdas?userId=%s";
        String faasUIUrl = "faasUI.com";
        return String.format(expectedUrl, faasUIUrl, accountId, userId);
    }

    private String getExpectedRequestBody(long timestamp, String headers, String payload) throws Exception {
        String expectedBody = "{\"timestamp\":%d,\"headers\":%s,\"payload\":%s}";
        return String.format(expectedBody, timestamp, headers, payload);
    }

    private String getMockResponse() {
        return "[\n" +
                "  {\n" +
                "    \"uuid\": \"6d0372f3-524c-4fe9-a9a4-bf0157c9deac\",\n" +
                "    \"version\": 1,\n" +
                "    \"name\": \"Sergey_Test\",\n" +
                "    \"description\": \"Test\",\n" +
                "    \"samplePayload\": {\n" +
                "      \"headers\": [],\n" +
                "      \"payload\": {}\n" +
                "    },\n" +
                "    \"state\": \"Productive\",\n" +
                "    \"runtime\": {\n" +
                "      \"uuid\": \"57732DA8-24F3-486B-9582-2F2F8C2AF43D\",\n" +
                "      \"name\": \"Node.js 10\",\n" +
                "      \"baseImageName\": \"lp-building-block_snapshot/lp-openfaas-lambda-node-base-image:latest\"\n"
                +
                "    },\n" +
                "    \"createdBy\": \"2851393312\",\n" +
                "    \"updatedBy\": \"2851393312\",\n" +
                "    \"createdAt\": \"2019-07-22T20:51:28.000Z\",\n" +
                "    \"updatedAt\": \"2019-07-22T21:02:33.000Z\",\n" +
                "    \"lastDeployment\": {\n" +
                "      \"uuid\": \"31B65DAA-A5E7-4B73-BC9F-C30F1380571D\",\n" +
                "      \"name\": \"stoic_hopper6\",\n" +
                "      \"lambdaUUID\": \"6d0372f3-524c-4fe9-a9a4-bf0157c9deac\",\n" +
                "      \"lambdaVersion\": 1,\n" +
                "      \"createdAt\": \"2019-07-22T20:51:28.000Z\",\n" +
                "      \"deployedAt\": \"2019-07-22T21:02:33.000Z\",\n" +
                "      \"createdBy\": \"2851393312\",\n" +
                "      \"imageName\": \"lpcr.int.liveperson.net/faas/lp-tlv-6d0372f3-524c-4fe9-a9a4-bf0157c9deac:1\","
                +
                "\n" +
                "      \"deploymentState\": \"Deploy Finish\"\n" +
                "    },\n" +
                "    \"implementation\": {\n" +
                "      \"code\": \"function lambda(input, callback) {\\n    callback(null, `Hello World`);\\n}\",\n"
                +
                "      \"dependencies\": [],\n" +
                "      \"environmentVariables\": []\n" +
                "    }\n" +
                "  }\n" +
                "]";
    }

    private EventResponse getExpectedResponse() throws ParseException {
        EventResponse expectedResponse = new EventResponse();
        expectedResponse.uuid = lambdaUUID;
        expectedResponse.timestamp = mockDateFormat.parse("2017-07-09");
        expectedResponse.result = new UUIDResponse();
        expectedResponse.result.key = "responseKey";
        expectedResponse.result.value = "responseValue";
        return expectedResponse;
    }
}
