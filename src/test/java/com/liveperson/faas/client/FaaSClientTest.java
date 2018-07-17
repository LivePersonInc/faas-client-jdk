package com.liveperson.faas.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liveperson.csds.client.CsdsWebClient;
import com.liveperson.faas.dto.FaaSInvocation;
import com.liveperson.faas.exception.FaaSException;
import com.liveperson.faas.http.RestClient;
import com.liveperson.faas.security.DefaultOAuthSignaturBuilder;
import com.liveperson.faas.security.OAuthSignaturBuilder;
import com.liveperson.faas.util.EventResponse;
import com.liveperson.faas.util.UUIDResponse;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class FaaSClientTest {

    @Mock
    ResponseEntity<String> responseEntityMock;

    @Mock
    RestClient restClientMock;

    @Captor
    ArgumentCaptor<Map<String, String>> httpHeaderCaptor;

    @Captor
    ArgumentCaptor<String> httpBodyCaptor;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SimpleDateFormat mockDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final String apiKey = "a6e8ddd8995344cb8a8373a060958e7a";
    private final String apiSecret = "a13d4dee1f7ab6e0";
    private final OAuthSignaturBuilder oAuthSignaturBuilder = new DefaultOAuthSignaturBuilder(apiKey, apiSecret);
    private String apiVersion = "1";
    private String externalSystem = "test_system";
    private String accountId = "le49829325";
    private FaaSEvent event = FaaSEvent.DenverPostSurveyEmailTranscript;
    private String lambdaUUID = "81ec57ed-b353-4c71-8543-423364db169d";
    private String gatewayUrl = "192.168.21.129:8080";

    private String getExpectedUUIDUrl() {
        String expectedUrl = "https://%s/api/account/%s/lambdas/%s/invoke?userId=%s&v=%s";
        return String.format(expectedUrl, gatewayUrl, accountId, lambdaUUID, externalSystem, apiVersion);
    }

    private String getExpectedEventUrl() {
        String expectedUrl = "https://%s/api/account/%s/events/%s/invoke?userId=%s&v=%s";
        return String.format(expectedUrl, gatewayUrl, accountId, event, externalSystem, apiVersion);
    }

    private String getExpectedRequestBody(long timestamp, String headers, String payload) throws Exception {
        String expectedBody = "{\"timestamp\":%d,\"headers\":%s,\"payload\":%s}";
        return String.format(expectedBody, timestamp, headers, payload);
    }

    @Before
    public void before(){
        this.mockDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
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
        when(restClientMock.post(eq(getExpectedUUIDUrl()), httpHeaderCaptor.capture(), httpBodyCaptor.capture())).thenReturn("\"lambda_result\"");

        //Create faas client instance
        FaaSClient client = new FaaSWebClient(gatewayUrl, restClientMock, oAuthSignaturBuilder);

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
        when(restClientMock.post(eq(getExpectedUUIDUrl()), httpHeaderCaptor.capture(), httpBodyCaptor.capture())).thenReturn("\"lambda_result\"");

        //Create faas client instance
        FaaSClient client = new FaaSWebClient(gatewayUrl, restClientMock, oAuthSignaturBuilder);

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
        when(restClientMock.post(eq(getExpectedUUIDUrl()), httpHeaderCaptor.capture(),
                httpBodyCaptor.capture())).thenReturn("{\"key\":\"responseKey\",\"value\":\"responseValue\"}");

        //Create faas client instance
        FaaSClient client = new FaaSWebClient(gatewayUrl, restClientMock, oAuthSignaturBuilder);

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

        when(restClientMock.post(eq(getExpectedEventUrl()), httpHeaderCaptor.capture(),
                httpBodyCaptor.capture())).thenReturn(mockResponse);

        //Create faas client instance
        FaaSClient client = new FaaSWebClient(gatewayUrl, restClientMock, oAuthSignaturBuilder);

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
        EventResponse[] response = client.invoke(externalSystem, accountId, FaaSEvent.DenverPostSurveyEmailTranscript, invocationData, EventResponse[].class);

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

    @Test(expected = FaaSException.class)
    public void throwFaaSExceptionAtHttpErrorForEventInvoke() throws Exception{
        //#############
        //# Prepare
        //#############
        //Set test specific data
        long timestamp = System.currentTimeMillis();

        //Set method mocks for mock objects
        //Mock the lambda invocation
        when(restClientMock.post(eq(getExpectedEventUrl()), httpHeaderCaptor.capture(), httpBodyCaptor.capture()))
                .thenThrow(new IOException("Error during rest call."));

        //Create faas client instance
        FaaSClient client = new FaaSWebClient(gatewayUrl, restClientMock, oAuthSignaturBuilder);

        //Create invocation data object
        FaaSInvocation<String> invocationData = new FaaSInvocation<String>(null, null);
        invocationData.setTimestamp(timestamp);

        //#############
        //# Execute
        //#############
        client.invoke(externalSystem, accountId, event, invocationData, EventResponse[].class);
    }

    @Test(expected = FaaSException.class)
    public void throwFaaSExceptionAtHttpError() throws Exception{
        //#############
        //# Prepare
        //#############
        //Set test specific data
        long timestamp = System.currentTimeMillis();

        //Set method mocks for mock objects
        //Mock the lambda invocation
        when(restClientMock.post(eq(getExpectedUUIDUrl()), httpHeaderCaptor.capture(), httpBodyCaptor.capture()))
                .thenThrow(new IOException("Error during rest call."));

        //Create faas client instance
        FaaSClient client = new FaaSWebClient(gatewayUrl, restClientMock, oAuthSignaturBuilder);

        //Create invocation data object
        FaaSInvocation<String> invocationData = new FaaSInvocation<String>(null, null);
        invocationData.setTimestamp(timestamp);

        //#############
        //# Execute
        //#############
        client.invoke(externalSystem, accountId, lambdaUUID, invocationData, String.class);
    }

    @Test(expected = FaaSException.class)
    public void throwFaaSExceptionAtOtherError() throws Exception{
        //#############
        //# Prepare
        //#############
        //Set test specific data
        long timestamp = System.currentTimeMillis();

        //Set method mocks for mock objects
        //Mock the lambda invocation
        when(restClientMock.post(eq(getExpectedUUIDUrl()), httpHeaderCaptor.capture(), httpBodyCaptor.capture()))
                .thenThrow(NullPointerException.class);

        //Create faas client instance
        FaaSClient client = new FaaSWebClient(gatewayUrl, restClientMock, oAuthSignaturBuilder);

        //Create invocation data object
        FaaSInvocation<String> invocationData = new FaaSInvocation<String>(null, null);
        invocationData.setTimestamp(timestamp);

        //#############
        //# Execute
        //#############
        client.invoke(externalSystem, accountId, lambdaUUID, invocationData, String.class);
    }

    @Test
    public void getInstanceViaCSDSDomain() throws Exception {
        FaaSWebClient.getInstance("192.168.21.129:8080", apiKey, apiSecret);
    }

    @Test
    public void getInstanceViaHostPort() throws Exception {
        FaaSWebClient.getInstance("localhost", 9902, apiKey, apiSecret);
    }

    @Test
    public void getInstanceViaCSDSClient() throws Exception {
        FaaSWebClient.getInstance(CsdsWebClient.getInstance(), apiKey, apiSecret);
    }
}
