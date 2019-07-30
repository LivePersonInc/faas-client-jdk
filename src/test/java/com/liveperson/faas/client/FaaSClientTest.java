package com.liveperson.faas.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liveperson.csds.api.BaseURIData;
import com.liveperson.csds.client.CsdsWebClient;
import com.liveperson.csds.client.api.CsdsClient;
import com.liveperson.faas.dto.FaaSInvocation;
import com.liveperson.faas.exception.FaaSException;
import com.liveperson.faas.http.RestClient;
import com.liveperson.faas.response.lambda.LambdaResponse;
import com.liveperson.faas.security.DefaultOAuthSignaturBuilder;
import com.liveperson.faas.security.OAuthSignaturBuilder;
import com.liveperson.faas.util.EventResponse;
import com.liveperson.faas.util.UUIDResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class FaaSClientTest {

    @Mock
    RestClient restClientMock;

    @Mock
    CsdsClient csdsClientMock;

    @Captor
    ArgumentCaptor<Map<String, String>> httpHeaderCaptor;

    @Captor
    ArgumentCaptor<String> httpBodyCaptor;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SimpleDateFormat mockDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final String apiKey = "dummyKey";
    private final String apiSecret = "dummySecret";
    private final OAuthSignaturBuilder oAuthSignaturBuilder = new DefaultOAuthSignaturBuilder(apiKey, apiSecret);
    private String apiVersion = "1";
    private String externalSystem = "test_system";
    private String accountId = "11111111";
    private String userId = "0051393312";
    private FaaSEvent event = FaaSEvent.ChatPostSurveyEmailTranscript;
    private String lambdaUUID = "81ec57ed-b353-4c71-8543-423364db169d";
    private String faasEVGUrl = "faasGW.com";
    private String faasGWUrl = "faasUI.com";

    private String getExpectedInvokeUUIDUrl() {
        String expectedUrl = "https://%s/api/account/%s/lambdas/%s/invoke?externalSystem=%s&v=%s";
        return String.format(expectedUrl, faasEVGUrl, accountId, lambdaUUID, externalSystem, apiVersion);
    }

    private String getExpectedInvokeEventUrl() {
        String expectedUrl = "https://%s/api/account/%s/events/%s/invoke?externalSystem=%s&v=%s";
        return String.format(expectedUrl, faasEVGUrl, accountId, event, externalSystem, apiVersion);
    }

    private String getExpectedIsImplementedUrl() {
        String expectedUrl = "https://%s/api/account/%s/events/%s/isImplemented?externalSystem=%s&v=%s";
        return String.format(expectedUrl, faasEVGUrl, accountId, event, externalSystem, apiVersion);
    }

    private String getExpectedLambdasOfAnAccountUrl() {
        String expectedUrl = "https://%s/api/account/%s/lambdas?v=%s&userId=%s";
        return String.format(expectedUrl, faasGWUrl, accountId, apiVersion, userId);
    }

    private String getExpectedRequestBody(long timestamp, String headers, String payload) throws Exception {
        String expectedBody = "{\"timestamp\":%d,\"headers\":%s,\"payload\":%s}";
        return String.format(expectedBody, timestamp, headers, payload);
    }

    @Before
    public void before() throws Exception{
        mockDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        when(csdsClientMock.get(eq(accountId), eq(FaaSWebClient.CSDS_EVG_SERVICE_NAME))).thenReturn(new BaseURIData(FaaSWebClient.CSDS_EVG_SERVICE_NAME, accountId, faasEVGUrl));
        when(csdsClientMock.get(eq(accountId), eq(FaaSWebClient.CSDS_GW_SERVICE_NAME))).thenReturn(new BaseURIData(FaaSWebClient.CSDS_EVG_SERVICE_NAME, accountId, faasGWUrl));
    }

    @Test
    public void stringInvocationViaUUIDTest() throws Exception {
        //#############
        //# Prepare
        //#############
        //Set test specific data
        String payload = "request_data";
        long timestamp = System.currentTimeMillis();

        //Set method mocks for mock objects
        //Mock the lambda invocation
        when(restClientMock.post(eq(getExpectedInvokeUUIDUrl()), httpHeaderCaptor.capture(), httpBodyCaptor.capture())).thenReturn("\"lambda_result\"");

        //Create faas client instance
        FaaSClient client = new FaaSWebClient(csdsClientMock, restClientMock, oAuthSignaturBuilder);

        //Create invocation data object
        FaaSInvocation<String> invocationData = new FaaSInvocation<String>(null, payload);
        invocationData.setTimestamp(timestamp);

        //#############
        //# Execute
        //#############
        String response = client.invoke(externalSystem, accountId, lambdaUUID, invocationData, String.class);

        //#############
        //# Verify
        //#############
        assertEquals("Lambda invocation with the wrong body",
                getExpectedRequestBody(timestamp, "[]", "\"request_data\""), httpBodyCaptor.getValue());

        assertTrue("Lambda invocation with wrong authorization header", httpHeaderCaptor.getValue().get("Authorization").contains("OAuth"));

        assertEquals("Lambda invocation result does not match expected value",
                "lambda_result", response);
    }

    @Test
    public void stringInvocationWithoutPayloadUUIDTest() throws Exception {
        //#############
        //# Prepare
        //#############
        //Set test specific data
        long timestamp = System.currentTimeMillis();

        //Set method mocks for mock objects
        //Mock the lambda invocation
        when(restClientMock.post(eq(getExpectedInvokeUUIDUrl()), httpHeaderCaptor.capture(), httpBodyCaptor.capture())).thenReturn("\"lambda_result\"");

        //Create faas client instance
        FaaSClient client = new FaaSWebClient(csdsClientMock, restClientMock, oAuthSignaturBuilder);

        //Create invocation data object
        FaaSInvocation<Object> invocationData = new FaaSInvocation<Object>(null, null);
        invocationData.setTimestamp(timestamp);

        //#############
        //# Execute
        //#############
        String response = client.invoke(externalSystem, accountId, lambdaUUID, invocationData, String.class);

        //#############
        //# Verify
        //#############
        assertEquals("Lambda invocation with the wrong body",
                getExpectedRequestBody(timestamp, "[]", "{}"), httpBodyCaptor.getValue());

        assertTrue("Lambda invocation with wrong authorization header", httpHeaderCaptor.getValue().get("Authorization").contains("OAuth"));

        assertEquals("Lambda invocation result does not match expected value",
                "lambda_result", response);
    }

    @Test
    public void objectInvocationViaUUIDTest() throws Exception {
        //#############
        //# Prepare
        //#############
        //Set test specific data
        UUIDResponse payload = new UUIDResponse();
        payload.key = "requestKey";
        payload.value = "requestValue";
        long timestamp = System.currentTimeMillis();

        //Set method mocks for mock objects
        when(restClientMock.post(eq(getExpectedInvokeUUIDUrl()), httpHeaderCaptor.capture(),
                httpBodyCaptor.capture())).thenReturn("{\"key\":\"responseKey\",\"value\":\"responseValue\"}");

        //Create faas client instance
        FaaSClient client = new FaaSWebClient(csdsClientMock, restClientMock, oAuthSignaturBuilder);

        //Create invocation data object
        FaaSInvocation<UUIDResponse> invocationData = new FaaSInvocation();
        Map<String, String> headers = new HashMap();
        headers.put("testHeader", "testHeaderValue");
        invocationData.setHeaders(headers);
        invocationData.setPayload(payload);
        invocationData.setTimestamp(timestamp);

        //#############
        //# Execute
        //#############
        UUIDResponse response = client.invoke(externalSystem, accountId, lambdaUUID, invocationData, UUIDResponse.class);

        //#############
        // # Verify
        //#############
        assertEquals("Lambda invocation with the wrong body",
                getExpectedRequestBody(timestamp, "[{\"key\":\"testHeader\",\"value\":\"testHeaderValue\"}]", "{\"key\":\"requestKey\",\"value\":\"requestValue\"}"),
                httpBodyCaptor.getValue());

        assertTrue("Lambda invocation with wrong authorization header", httpHeaderCaptor.getValue().get("Authorization").contains("OAuth"));

        UUIDResponse expectedResponse = new UUIDResponse();
        expectedResponse.key = "responseKey";
        expectedResponse.value = "responseValue";
        assertEquals("Lambda invocation result does not match expected value", expectedResponse.toString(), response.toString());
    }

    @Test
    public void objectInvocationViaEventTest() throws Exception {
        //#############
        //# Prepare
        //#############
        //Set test specific data
        UUIDResponse payload = new UUIDResponse();
        payload.key = "requestKey";
        payload.value = "requestValue";
        long timestamp = System.currentTimeMillis();

        //Set method mocks for mock objects
        String mockResponse = "[\n" +
                "  {\n" +
                "    \"uuid\": \"" + lambdaUUID + "\",\n" +
                "    \"timestamp\": \"2017-07-09\",\n" +
                "    \"result\": {\n" +
                "      \"key\": \"responseKey\",\n" +
                "      \"value\": \"responseValue\"\n" +
                "    }\n" +
                "  }\n" +
                "]";

        when(restClientMock.post(eq(getExpectedInvokeEventUrl()), httpHeaderCaptor.capture(),
                httpBodyCaptor.capture())).thenReturn(mockResponse);

        //Create faas client instance
        FaaSClient client = new FaaSWebClient(csdsClientMock, restClientMock, oAuthSignaturBuilder);

        //Create invocation data object
        FaaSInvocation<UUIDResponse> invocationData = new FaaSInvocation();
        Map<String, String> headers = new HashMap();
        headers.put("testHeader", "testHeaderValue");
        invocationData.setHeaders(headers);
        invocationData.setPayload(payload);
        invocationData.setTimestamp(timestamp);

        //#############
        //# Execute
        //#############
        EventResponse[] response = client.invoke(externalSystem, accountId, FaaSEvent.ChatPostSurveyEmailTranscript, invocationData, EventResponse[].class);

        //#############
        // # Verify
        //#############
        assertEquals("Lambda invocation with the wrong body",
                getExpectedRequestBody(timestamp, "[{\"key\":\"testHeader\",\"value\":\"testHeaderValue\"}]", "{\"key\":\"requestKey\",\"value\":\"requestValue\"}"),
                httpBodyCaptor.getValue());

        assertTrue("Lambda invocation with wrong authorization header", httpHeaderCaptor.getValue().get("Authorization").contains("OAuth"));

        EventResponse expectedResponse = new EventResponse();
        expectedResponse.uuid = lambdaUUID;
        expectedResponse.timestamp = mockDateFormat.parse("2017-07-09");
        expectedResponse.result = new UUIDResponse();
        expectedResponse.result.key = "responseKey";
        expectedResponse.result.value = "responseValue";
        assertEquals("Lambda invocation result does not match expected value", expectedResponse.toString(), response[0].toString());
    }

    @Test
    public void getLambdasOfAnAccountTest() throws Exception {
        //#############
        //# Prepare
        //#############

        //Set method mocks for mock objects
        String mockResponse = "[\n" +
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
                "      \"baseImageName\": \"lp-building-block_snapshot/lp-openfaas-lambda-node-base-image:latest\"\n" +
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
                "      \"imageName\": \"lpcr.int.liveperson.net/faas/lp-tlv-6d0372f3-524c-4fe9-a9a4-bf0157c9deac:1\",\n" +
                "      \"deploymentState\": \"Deploy Finish\"\n" +
                "    },\n" +
                "    \"implementation\": {\n" +
                "      \"code\": \"function lambda(input, callback) {\\n    callback(null, `Hello World`);\\n}\",\n" +
                "      \"dependencies\": [],\n" +
                "      \"environmentVariables\": []\n" +
                "    }\n" +
                "  }\n" +
                "]";

        when(restClientMock.get(eq(getExpectedLambdasOfAnAccountUrl()), httpHeaderCaptor.capture()))
                .thenReturn(mockResponse);

        //Create faas client instance
        FaaSClient client = new FaaSWebClient(csdsClientMock, restClientMock, oAuthSignaturBuilder);

        //#############
        //# Execute
        //#############
        List<LambdaResponse> actualResponse = client.getLambdasOfAccount(accountId, userId, new HashMap<String, String>());

        //#############
        // # Verify
        //#############
        List<LambdaResponse> expectedResponse = objectMapper.readValue(mockResponse, new TypeReference<List<LambdaResponse>>() { });
        assertEquals(expectedResponse.get(0), actualResponse.get(0));
    }

    @Test(expected = FaaSException.class)
    public void throwFaaSExceptionAtHttpErrorForEventInvoke() throws Exception{
        //#############
        //# Prepare
        //#############
        //Set test specific data
        long timestamp = System.currentTimeMillis();

        //Set method mocks for mock objects
        //Mock the lambda invocation
        when(restClientMock.post(eq(getExpectedInvokeEventUrl()), httpHeaderCaptor.capture(), httpBodyCaptor.capture()))
                .thenThrow(new IOException("Error during rest call."));

        //Create faas client instance
        FaaSClient client = new FaaSWebClient(csdsClientMock, restClientMock, oAuthSignaturBuilder);

        //Create invocation data object
        FaaSInvocation<String> invocationData = new FaaSInvocation<String>(null, null);
        invocationData.setTimestamp(timestamp);

        //#############
        //# Execute
        //#############
        client.invoke(externalSystem, accountId, event, invocationData, EventResponse[].class);
    }

    @Test(expected = FaaSException.class)
    public void throwFaaSExceptionAtHttpErrorForUUIDInvoke() throws Exception{
        //#############
        //# Prepare
        //#############
        //Set test specific data
        long timestamp = System.currentTimeMillis();

        //Set method mocks for mock objects
        //Mock the lambda invocation
        when(restClientMock.post(eq(getExpectedInvokeUUIDUrl()), httpHeaderCaptor.capture(), httpBodyCaptor.capture()))
                .thenThrow(new IOException("Error during rest call."));

        //Create faas client instance
        FaaSClient client = new FaaSWebClient(csdsClientMock, restClientMock, oAuthSignaturBuilder);

        //Create invocation data object
        FaaSInvocation<String> invocationData = new FaaSInvocation<String>(null, null);
        invocationData.setTimestamp(timestamp);

        //#############
        //# Execute
        //#############
        client.invoke(externalSystem, accountId, lambdaUUID, invocationData, String.class);
    }

    @Test(expected = FaaSException.class)
    public void throwFaaSExceptionAtOtherErrorForUUIDInvoke() throws Exception{
        //#############
        //# Prepare
        //#############
        //Set test specific data
        long timestamp = System.currentTimeMillis();

        //Set method mocks for mock objects
        //Mock the lambda invocation
        when(restClientMock.post(eq(getExpectedInvokeUUIDUrl()), httpHeaderCaptor.capture(), httpBodyCaptor.capture()))
                .thenThrow(NullPointerException.class);

        //Create faas client instance
        FaaSClient client = new FaaSWebClient(csdsClientMock, restClientMock, oAuthSignaturBuilder);

        //Create invocation data object
        FaaSInvocation<String> invocationData = new FaaSInvocation<String>(null, null);
        invocationData.setTimestamp(timestamp);

        //#############
        //# Execute
        //#############
        client.invoke(externalSystem, accountId, lambdaUUID, invocationData, String.class);
    }

    @Test
    public void checkIfEventIsImplementedTest() throws Exception {
        //#############
        //# Prepare
        //#############

        //Set method mocks for mock objects
        //Mock the isImplemented call
        when(restClientMock.get(eq(getExpectedIsImplementedUrl()), httpHeaderCaptor.capture())).thenReturn("{\"implemented\": true}");

        //Create faas client instance
        FaaSClient client = new FaaSWebClient(csdsClientMock, restClientMock, oAuthSignaturBuilder);

        //#############
        //# Execute
        //#############
        boolean isImplemented = client.isImplemented(externalSystem, accountId, event);

        //#############
        //# Verify
        //#############
        assertTrue("Lambda should be implemented", isImplemented);
    }

    @Test(expected = FaaSException.class)
    public void throwFaaSExceptionAtIsImplemented() throws Exception {
        //#############
        //# Prepare
        //#############

        //Set method mocks for mock objects
        //Raise exception during call
        when(restClientMock.get(eq(getExpectedIsImplementedUrl()), httpHeaderCaptor.capture()))
                .thenThrow(new IOException("Error during rest call."));

        //Create faas client instance
        FaaSClient client = new FaaSWebClient(csdsClientMock, restClientMock, oAuthSignaturBuilder);

        //#############
        //# Execute
        //#############
        boolean isImplemented = client.isImplemented(externalSystem, accountId, event);
    }

    @Test
    public void getInstanceViaCSDSDomain() throws Exception {
        FaaSWebClient.getInstance("192.168.21.129:8080", apiKey, apiSecret);
    }

    @Test
    public void getInstanceViaCSDSClient() throws Exception {
        FaaSWebClient.getInstance(CsdsWebClient.getInstance(), apiKey, apiSecret);
    }
}
