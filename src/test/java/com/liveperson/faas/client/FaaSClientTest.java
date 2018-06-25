package com.liveperson.faas.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liveperson.faas.dto.FaaSInvocation;
import com.liveperson.faas.exception.FaaSException;
import com.liveperson.faas.security.DefaultOAuthSignaturBuilder;
import com.liveperson.faas.security.OAuthSignaturBuilder;
import com.liveperson.faas.util.Dummy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class FaaSClientTest {

    @Mock
    ResponseEntity<String> responseEntityMock;

    @Mock
    RestTemplate restTemplateMock;

    @Captor
    ArgumentCaptor<HttpEntity> httpEntityCaptor;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey = "a6e8ddd8995344cb8a8373a060958e7a";
    private final String apiSecret = "a13d4dee1f7ab6e0";
    private final OAuthSignaturBuilder oAuthSignaturBuilder = new DefaultOAuthSignaturBuilder(apiKey, apiSecret);
    private String apiVersion = "1";
    private String externalSystem = "test_system";
    private String accountId = "le49829325";
    private String lambdaUUID = "81ec57ed-b353-4c71-8543-423364db169d";
    private String gatewayUrl = "192.168.21.129:8080";

    private String getExpectedUrl() {
        String expectedUrl = "http://%s/api/account/%s/lambdas/%s/invoke?userId=%s&v=%s";
        return String.format(expectedUrl, gatewayUrl, accountId, lambdaUUID, externalSystem, apiVersion);
    }

    private String getExpectedRequestBody(long timestamp, String headers, String payload) throws Exception {
        String expectedBody = "{\"timestamp\":%d,\"headers\":%s,\"payload\":%s}";
        return String.format(expectedBody, timestamp, headers, payload);
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
        //Mock the lambda invocation - response body
        when(responseEntityMock.getBody()).thenReturn("\"lambda_result\"");
        //Mock the lambda invocation
        when(restTemplateMock.exchange(eq(getExpectedUrl()), eq(HttpMethod.POST), httpEntityCaptor.capture(), eq(String.class)))
                .thenReturn(responseEntityMock);

        //Create faas client instance
        FaaSClient client = new FaaSWebClient(gatewayUrl, restTemplateMock, oAuthSignaturBuilder);

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
                getExpectedRequestBody(timestamp, "[]", "\"request_data\""), httpEntityCaptor.getValue().getBody());

        assertTrue("Lambda invocation with wrong authorization header", httpEntityCaptor.getValue().getHeaders().get("Authorization").get(0).contains("OAuth"));

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
        //Mock the lambda invocation - response body
        when(responseEntityMock.getBody()).thenReturn("\"lambda_result\"");
        //Mock the lambda invocation
        when(restTemplateMock.exchange(eq(getExpectedUrl()), eq(HttpMethod.POST), httpEntityCaptor.capture(), eq(String.class)))
                .thenReturn(responseEntityMock);

        //Create faas client instance
        FaaSClient client = new FaaSWebClient(gatewayUrl, restTemplateMock, oAuthSignaturBuilder);

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
                getExpectedRequestBody(timestamp, "[]", "{}"), httpEntityCaptor.getValue().getBody());

        assertTrue("Lambda invocation with wrong authorization header", httpEntityCaptor.getValue().getHeaders().get("Authorization").get(0).contains("OAuth"));

        assertEquals("Lambda invocation result does not match expected value",
                "lambda_result", response);
    }

    @Test
    public void objectInvocationViaUUIDTest() throws Exception {
        //#############
        //# Prepare
        //#############
        //Set test specific data
        Dummy payload = new Dummy();
        payload.key = "requestKey";
        payload.value = "requestValue";
        long timestamp = System.currentTimeMillis();

        //Set method mocks for mock objects
        when(responseEntityMock.getBody()).thenReturn("{\"key\":\"responseKey\",\"value\":\"responseValue\"}");
        when(restTemplateMock.exchange(eq(getExpectedUrl()), eq(HttpMethod.POST), httpEntityCaptor.capture(), eq(String.class)))
                .thenReturn(responseEntityMock);

        //Create faas client instance
        FaaSClient client = new FaaSWebClient(gatewayUrl, restTemplateMock, oAuthSignaturBuilder);

        //Create invocation data object
        FaaSInvocation<Dummy> invocationData = new FaaSInvocation();
        Map<String, String> headers = new HashMap();
        headers.put("testHeader", "testHeaderValue");
        invocationData.setHeaders(headers);
        invocationData.setPayload(payload);
        invocationData.setTimestamp(timestamp);

        //#############
        //# Execute
        //#############
        Dummy response = client.invoke(externalSystem, accountId, lambdaUUID, invocationData, Dummy.class);

        //#############
        // # Verify
        //#############
        assertEquals("Lambda invocation with the wrong body",
                getExpectedRequestBody(timestamp, "[{\"key\":\"testHeader\",\"value\":\"testHeaderValue\"}]", "{\"key\":\"requestKey\",\"value\":\"requestValue\"}"),
                httpEntityCaptor.getValue().getBody());

        assertTrue("Lambda invocation with wrong authorization header", httpEntityCaptor.getValue().getHeaders().get("Authorization").get(0).contains("OAuth"));

        Dummy expectedResponse = new Dummy();
        expectedResponse.key = "responseKey";
        expectedResponse.value = "responseValue";
        assertEquals("Lambda invocation result does not match expected value", expectedResponse.toString(), response.toString());
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
        when(restTemplateMock.exchange(eq(getExpectedUrl()), eq(HttpMethod.POST), httpEntityCaptor.capture(), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error", "body".getBytes(), null));

        //Create faas client instance
        FaaSClient client = new FaaSWebClient(gatewayUrl, restTemplateMock, oAuthSignaturBuilder);

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
        when(restTemplateMock.exchange(eq(getExpectedUrl()), eq(HttpMethod.POST), httpEntityCaptor.capture(), eq(String.class)))
                .thenThrow(NullPointerException.class);

        //Create faas client instance
        FaaSClient client = new FaaSWebClient(gatewayUrl, restTemplateMock, oAuthSignaturBuilder);

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
}
