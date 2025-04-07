package com.liveperson.faas.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liveperson.faas.client.types.FaaSEventImplementedExpiry;
import com.liveperson.faas.client.types.OptionalParams;
import com.liveperson.faas.csds.CsdsClient;
import com.liveperson.faas.dto.FaaSError;
import com.liveperson.faas.dto.FaaSErrorV1;
import com.liveperson.faas.dto.FaaSInvocation;
import com.liveperson.faas.exception.*;
import com.liveperson.faas.http.RestClient;
import com.liveperson.faas.metriccollector.MetricCollector;
import com.liveperson.faas.response.lambda.FunctionResponse;
import com.liveperson.faas.security.AuthDPoPSignatureBuilder;
import com.liveperson.faas.security.AuthSignatureBuilder;
import com.liveperson.faas.util.EventResponse;
import com.liveperson.faas.util.UUIDResponse;
import org.junit.Before;
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
        public void isV2Domain() throws Exception {
                Boolean isV2 = client.isV2Domain();
                assertTrue(isV2);
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

        @Test
        public void invokeWithUUIDWithoutResponse() throws IOException, FaaSException {
                Map<String, String> headers = getHeaders();
                FaaSInvocation<String> faaSInvocation = getStringFaaSInvocation(headers);

                client.invokeByUUID(lpEventSource, lambdaUUID, faaSInvocation, optionalParams);

                verify(metricCollectorMock, times(1)).onInvokeByUUIDSuccess(eq(lpEventSource), anyFloat(),
                                eq(lambdaUUID),
                                eq(accountId));
                verify(restClientMock, times(1)).post(getExpectedInvokeUUIDUrl(), headers,
                                faaSInvocation.toString(), optionalParams.getTimeOutInMs());
        }

        @Test
        public void invokeWithFaaSEventWithRequestIdWithoutResponse() throws IOException, FaaSException {
                Map<String, String> headers = getHeaders();
                FaaSInvocation<String> faaSInvocation = getStringFaaSInvocation(headers);
                optionalParams.setRequestId(requestId);

                client.invokeByEvent(lpEventSource, event, faaSInvocation, optionalParams);

                verify(metricCollectorMock, times(1)).onInvokeByEventSuccess(eq(lpEventSource), anyFloat(),
                                eq(event.toString()), eq(accountId));
                verify(restClientMock, times(1)).post(getExpectedInvokeEventUrl(), headers,
                                faaSInvocation.toString(), optionalParams.getTimeOutInMs());
        }

        @Test
        public void invokeWithFaaSEventWithRequestIdAndEventStringWithoutResponse() throws IOException, FaaSException {
                Map<String, String> headers = getHeaders();
                FaaSInvocation<String> faaSInvocation = getStringFaaSInvocation(headers);
                optionalParams.setRequestId(requestId);

                client.invokeByEvent(lpEventSource, event.toString(), faaSInvocation, optionalParams);

                verify(metricCollectorMock, times(1)).onInvokeByEventSuccess(eq(lpEventSource), anyFloat(),
                                eq(event.toString()), eq(accountId));
                verify(restClientMock, times(1)).post(getExpectedInvokeEventUrl(), headers,
                                faaSInvocation.toString(), optionalParams.getTimeOutInMs());
        }

        @Test(expected = FaaSDetailedException.class)
        public void invokeViaUUIDThrowsFaasDetailedException() throws IOException, FaaSException {
                try {
                        FaaSError faaSError = new FaaSError();
                        faaSError.setCode("faas.error.code");
                        faaSError.setMessage("My custom error.");

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

        @Test(expected = FaaSFunctionException.class)
        public void invokeViaUUIDThrowFaaSLambdaException() throws IOException, FaaSException {
                try {
                        long timestamp = System.currentTimeMillis();
                        FaaSInvocation<String> invocationData = new FaaSInvocation<String>(null, null);
                        invocationData.setTimestamp(timestamp);
                        FaaSError faaSError = new FaaSError();
                        faaSError.setCode(FaaSFunctionErrorCodes.CUSTOM_FAILURE.getCode());
                        faaSError.setMessage("My custom error.");

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

        @SuppressWarnings("unchecked")
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
        public void invokeViaEventType() throws Exception {
                UUIDResponse payload = new UUIDResponse();
                payload.key = "requestKey";
                payload.value = "requestValue";
                long timestamp = System.currentTimeMillis();
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
                Map<String, String> headers = getTestHeaders();
                FaaSInvocation<UUIDResponse> invocationData = getUUIDResponseFaaSInvocation(payload, timestamp,
                                headers);
                EventResponse expectedResponse = getExpectedResponse();

                when(restClientMock.post(eq(getExpectedInvokeEventUrl()), httpHeaderCaptor.capture(),
                                httpBodyCaptor.capture(), eq(defaultTimeOut))).thenReturn(mockResponse);
                EventResponse[] response = client.invokeByEvent(lpEventSource,
                                FaaSEvent.ChatPostSurveyEmailTranscript,
                                invocationData, EventResponse[].class, optionalParams);

                verify(metricCollectorMock, times(1)).onInvokeByEventSuccess(eq(lpEventSource), anyInt(),
                                eq(event.toString()),
                                eq(accountId));
                assertEquals("Lambda invocation with the wrong body",
                                getExpectedRequestBody(timestamp,
                                                "[{\"key\":\"testHeader\",\"value\":\"testHeaderValue\"}]", "{\"key" +
                                                                "\":\"requestKey\",\"value\":\"requestValue\"}"),
                                httpBodyCaptor.getValue());
                assertTrue("Lambda invocation with wrong authorization header", httpHeaderCaptor.getValue().get(
                                "Authorization").contains("Bearer"));
                assertEquals("Lambda invocation result does not match expected value", expectedResponse.toString(),
                                response[0].toString());
                assertTrue(httpHeaderCaptor.getValue().get(
                                "LP-EventSource").contains(lpEventSource));
        }

        @Test
        public void invokeViaEventTypeWithAuthDPoP() throws Exception {
                UUIDResponse payload = new UUIDResponse();
                payload.key = "requestKey";
                payload.value = "requestValue";
                long timestamp = System.currentTimeMillis();
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
                Map<String, String> headers = getTestHeaders();
                FaaSInvocation<UUIDResponse> invocationData = getUUIDResponseFaaSInvocation(payload, timestamp,
                                headers);
                EventResponse expectedResponse = getExpectedResponse();

                when(restClientMock.post(eq(getExpectedInvokeEventUrl()), httpHeaderCaptor.capture(),
                                httpBodyCaptor.capture(), eq(defaultTimeOut))).thenReturn(mockResponse);
                EventResponse[] response = clientWithDPoP.invokeByEvent(lpEventSource,
                                FaaSEvent.ChatPostSurveyEmailTranscript,
                                invocationData, EventResponse[].class, optionalParams);

                verify(metricCollectorMock, times(1)).onInvokeByEventSuccess(eq(lpEventSource), anyInt(),
                                eq(event.toString()),
                                eq(accountId));
                verify(authDPoPSignatureBuilder, times(1)).getAccessTokenInternal(eq("https://" + faasGWUrl));
                verify(authDPoPSignatureBuilder, times(1)).getDpopHeaderInternal(
                                eq(getExpectedInvokeEventUrl()),
                                eq("POST"), eq(accessToken));
                assertEquals("Lambda invocation with the wrong body",
                                getExpectedRequestBody(timestamp,
                                                "[{\"key\":\"testHeader\",\"value\":\"testHeaderValue\"}]", "{\"key" +
                                                                "\":\"requestKey\",\"value\":\"requestValue\"}"),
                                httpBodyCaptor.getValue());
                assertTrue("Lambda invocation with wrong authorization header",
                                httpHeaderCaptor.getValue().get("Authorization").equals("DPoP " + accessToken));
                assertTrue("Lambda invocation with wrong DPoP header",
                                httpHeaderCaptor.getValue().get("DPoP").equals(dpopHeader));
                assertEquals("Lambda invocation result does not match expected value", expectedResponse.toString(),
                                response[0].toString());
                assertTrue(httpHeaderCaptor.getValue().get(
                                "LP-EventSource").contains(lpEventSource));
        }

        @Test
        public void invokeViaEventTypeWithEventString() throws Exception {
                UUIDResponse payload = new UUIDResponse();
                payload.key = "requestKey";
                payload.value = "requestValue";
                long timestamp = System.currentTimeMillis();
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
                Map<String, String> headers = getTestHeaders();
                FaaSInvocation<UUIDResponse> invocationData = getUUIDResponseFaaSInvocation(payload, timestamp,
                                headers);
                EventResponse expectedResponse = getExpectedResponse();

                when(restClientMock.post(eq(getExpectedInvokeEventUrl()), httpHeaderCaptor.capture(),
                                httpBodyCaptor.capture(), eq(defaultTimeOut))).thenReturn(mockResponse);
                EventResponse[] response = client.invokeByEvent(lpEventSource,
                                FaaSEvent.ChatPostSurveyEmailTranscript.toString(),
                                invocationData, EventResponse[].class, optionalParams);

                verify(metricCollectorMock, times(1)).onInvokeByEventSuccess(eq(lpEventSource), anyInt(),
                                eq(event.toString()),
                                eq(accountId));
                assertEquals("Lambda invocation with the wrong body",
                                getExpectedRequestBody(timestamp,
                                                "[{\"key\":\"testHeader\",\"value\":\"testHeaderValue\"}]", "{\"key" +
                                                                "\":\"requestKey\",\"value\":\"requestValue\"}"),
                                httpBodyCaptor.getValue());
                assertTrue("Lambda invocation with wrong authorization header", httpHeaderCaptor.getValue().get(
                                "Authorization").contains("Bearer"));
                assertEquals("Lambda invocation result does not match expected value", expectedResponse.toString(),
                                response[0].toString());
                assertTrue(httpHeaderCaptor.getValue().get(
                                "LP-EventSource").contains(lpEventSource));
        }

        @Test
        public void invokeViaEventTypeWithEventStringWithRequestId() throws Exception {
                UUIDResponse payload = new UUIDResponse();
                payload.key = "requestKey";
                payload.value = "requestValue";
                long timestamp = System.currentTimeMillis();
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
                Map<String, String> headers = getTestHeaders();
                FaaSInvocation<UUIDResponse> invocationData = getUUIDResponseFaaSInvocation(payload, timestamp,
                                headers);
                EventResponse expectedResponse = getExpectedResponse();
                optionalParams.setRequestId(requestId);

                when(restClientMock.post(eq(getExpectedInvokeEventUrl()), httpHeaderCaptor.capture(),
                                httpBodyCaptor.capture(), eq(defaultTimeOut))).thenReturn(mockResponse);
                EventResponse[] response = client.invokeByEvent(lpEventSource,
                                FaaSEvent.ChatPostSurveyEmailTranscript.toString(),
                                invocationData, EventResponse[].class, optionalParams);

                verify(metricCollectorMock, times(1)).onInvokeByEventSuccess(eq(lpEventSource), anyInt(),
                                eq(event.toString()),
                                eq(accountId));
                assertEquals("Lambda invocation with the wrong body",
                                getExpectedRequestBody(timestamp,
                                                "[{\"key\":\"testHeader\",\"value\":\"testHeaderValue\"}]", "{\"key" +
                                                                "\":\"requestKey\",\"value\":\"requestValue\"}"),
                                httpBodyCaptor.getValue());
                assertTrue("Lambda invocation with wrong authorization header", httpHeaderCaptor.getValue().get(
                                "Authorization").contains("Bearer"));
                assertEquals("Lambda invocation result does not match expected value", expectedResponse.toString(),
                                response[0].toString());
                assertTrue(httpHeaderCaptor.getValue().get(
                                "LP-EventSource").contains(lpEventSource));
        }

        @Test
        public void invokeViaEventTypeWithRequestId() throws Exception {
                UUIDResponse payload = new UUIDResponse();
                payload.key = "requestKey";
                payload.value = "requestValue";
                long timestamp = System.currentTimeMillis();
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
                Map<String, String> headers = getTestHeaders();
                FaaSInvocation<UUIDResponse> invocationData = getUUIDResponseFaaSInvocation(payload, timestamp,
                                headers);
                EventResponse expectedResponse = getExpectedResponse();
                optionalParams.setRequestId(requestId);

                when(restClientMock.post(eq(getExpectedInvokeEventUrl()), httpHeaderCaptor.capture(),
                                httpBodyCaptor.capture(), eq(defaultTimeOut))).thenReturn(mockResponse);
                EventResponse[] response = client.invokeByEvent(lpEventSource,
                                FaaSEvent.ChatPostSurveyEmailTranscript,
                                invocationData, EventResponse[].class, optionalParams);

                verify(metricCollectorMock, times(1)).onInvokeByEventSuccess(eq(lpEventSource), anyFloat(),
                                eq(event.toString()), eq(accountId));
                assertEquals("Lambda invocation with the wrong body",
                                getExpectedRequestBody(timestamp,
                                                "[{\"key\":\"testHeader\",\"value\":\"testHeaderValue\"}]", "{\"key" +
                                                                "\":\"requestKey\",\"value\":\"requestValue\"}"),
                                httpBodyCaptor.getValue());
                assertTrue("Lambda invocation with wrong authorization header", httpHeaderCaptor.getValue().get(
                                "Authorization").contains("Bearer"));
                assertTrue("Lambda invocation with wrong authorization header", httpHeaderCaptor.getValue().get(
                                "X-REQUEST-ID").contains(requestId));
                assertEquals("Lambda invocation result does not match expected value", expectedResponse.toString(),
                                response[0].toString());
                assertTrue(httpHeaderCaptor.getValue().get(
                                "LP-EventSource").contains(lpEventSource));
        }

        @Test(expected = FaaSException.class)
        public void invokeViaEventTypeThrowFaaSException() throws IOException, FaaSException {
                try {
                        long timestamp = System.currentTimeMillis();
                        FaaSInvocation<String> invocationData = new FaaSInvocation<String>(null, null);
                        invocationData.setTimestamp(timestamp);

                        when(restClientMock.post(eq(getExpectedInvokeEventUrl()), httpHeaderCaptor.capture(),
                                        httpBodyCaptor.capture(), eq(defaultTimeOut)))
                                        .thenThrow(new IOException("Error during rest call."));
                        client.invokeByEvent(lpEventSource, event, invocationData, EventResponse[].class,
                                        optionalParams);
                } catch (Exception ex) {
                        verify(metricCollectorMock, times(1)).onInvokeByEventFailure(eq(lpEventSource), anyFloat(),
                                        eq(event.toString()), eq(accountId), eq(-1), any());
                        throw ex;
                }
        }

        @Test(expected = FaaSFunctionException.class)
        public void invokeViaEventTypeThrowsFaaSLambdaException() throws IOException, FaaSException {
                try {
                        long timestamp = System.currentTimeMillis();
                        FaaSInvocation<String> invocationData = new FaaSInvocation<String>(null, null);
                        invocationData.setTimestamp(timestamp);
                        FaaSError faaSError = new FaaSError();
                        faaSError.setCode(FaaSFunctionErrorCodes.CUSTOM_FAILURE.getCode());
                        faaSError.setMessage("My custom error.");
                        when(restClientMock.post(eq(getExpectedInvokeEventUrl()), httpHeaderCaptor.capture(),
                                        httpBodyCaptor.capture(), eq(defaultTimeOut)))
                                        .thenThrow(new RestException("Error during rest call.",
                                                        objectMapper.writeValueAsString(faaSError),
                                                        500));
                        client.invokeByEvent(lpEventSource, event, invocationData, EventResponse[].class,
                                        optionalParams);
                } catch (Exception ex) {
                        verify(metricCollectorMock, times(1)).onInvokeByEventFailure(eq(lpEventSource), anyFloat(),
                                        eq(event.toString()), eq(accountId), eq(500), any());
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

        @Test
        public void getFunctions() throws Exception {
                String mockResponse = getMockResponse();

                when(restClientMock.get(eq(getExpectedFunctionsOfAnAccountUrl()), httpHeaderCaptor.capture(),
                                eq(defaultTimeOut)))
                                .thenReturn(mockResponse);
                List<FunctionResponse> actualResponse = client.getFunctions(userId, new HashMap<String, String>(),
                                optionalParams);

                List<FunctionResponse> expectedResponse = objectMapper.readValue(mockResponse,
                                new TypeReference<List<FunctionResponse>>() {
                                });

                System.out.println(actualResponse.get(0));
                verify(metricCollectorMock, times(1)).onGetFunctionsSuccess(eq(userId), anyFloat(), eq(accountId));
                assertEquals(expectedResponse.get(0), actualResponse.get(0));
        }

        @Test
        public void getFunctionsWithAuthDPoP() throws Exception {
                String mockResponse = getMockResponse();

                when(restClientMock.get(eq(getExpectedFunctionsOfAnAccountUrl()), httpHeaderCaptor.capture(),
                                eq(defaultTimeOut)))
                                .thenReturn(mockResponse);
                List<FunctionResponse> actualResponse = clientWithDPoP.getFunctions(userId,
                                new HashMap<String, String>(),
                                optionalParams);
                List<FunctionResponse> expectedResponse = objectMapper.readValue(mockResponse,
                                new TypeReference<List<FunctionResponse>>() {
                                });

                verify(metricCollectorMock, times(1)).onGetFunctionsSuccess(eq(userId), anyFloat(), eq(accountId));
                verify(authDPoPSignatureBuilder, times(1)).getAccessTokenInternal(eq("https://" + faasUIUrl));
                verify(authDPoPSignatureBuilder, times(1)).getDpopHeaderInternal(
                                eq(getExpectedFunctionsOfAnAccountUrl()),
                                eq("GET"), eq(accessToken));
                assertTrue("Lambda invocation with wrong authorization header",
                                httpHeaderCaptor.getValue().get("Authorization").equals("DPoP " + accessToken));
                assertTrue("Lambda invocation with wrong DPoP header",
                                httpHeaderCaptor.getValue().get("DPoP").equals(dpopHeader));
                assertEquals(expectedResponse.get(0), actualResponse.get(0));
        }

        @Test
        public void getFunctionsWithOptionalQueryParameters() throws IOException {
                FunctionResponse lambdaResponse = new FunctionResponse();
                FunctionResponse[] responses = new FunctionResponse[2];
                responses[0] = lambdaResponse;
                String myResponse = objectMapper.writeValueAsString(responses);
                HashMap<String, String> filterMap = new HashMap<String, String>();
                filterMap.put("state", "Productive");
                filterMap.put("eventId", "eventId");
                filterMap.put("name", "myFilter");
                String expectedUrl = String.format("https://faasUI.functions.com/api/account/%s/functions?userId=%s" +
                                "&state=Productive&eventId=eventId&name=myFilter", accountId, userId);

                when(restClientMock.get(urlCaptor.capture(), any(), anyInt())).thenReturn(myResponse);
                try {
                        client.getFunctions(userId, filterMap, optionalParams);
                } catch (FaaSException e) {
                }

                verify(metricCollectorMock, times(1)).onGetFunctionsSuccess(eq(userId), anyFloat(), eq(accountId));
                assertEquals(expectedUrl, urlCaptor.getValue());

        }

        @Test(expected = FaaSDetailedException.class)
        public void getFunctionsThrowFaaSDetailedException() throws Exception {
                try {
                        FaaSError faaSError = new FaaSError();
                        faaSError.setCode("faas.error.code");
                        faaSError.setMessage("My custom error.");

                        when(restClientMock.get(eq(getExpectedFunctionsOfAnAccountUrl()), anyMap(), eq(defaultTimeOut)))
                                        .thenThrow(new RestException("Error during call.",
                                                        objectMapper.writeValueAsString(faaSError),
                                                        500));

                        client.getFunctions(userId, new HashMap<String, String>(), optionalParams);
                } catch (Exception ex) {
                        verify(metricCollectorMock, times(1)).onGetFunctionsFailure(eq(userId), anyFloat(),
                                        eq(accountId),
                                        eq(500),
                                        any());
                        throw ex;
                }
        }

        @Test(expected = FaaSException.class)
        public void getFunctionsThrowFaaSExceptionIfResponseNotParsable() throws IOException, FaaSException {
                try {
                        when(restClientMock.get(eq(getExpectedFunctionsOfAnAccountUrl()), httpHeaderCaptor.capture(),
                                        eq(defaultTimeOut)))
                                        .thenThrow(new RestException("Error during call.",
                                                        "This is an unexpected error response.", 500));
                        client.getFunctions(userId, new HashMap<String, String>(), optionalParams);
                } catch (Exception ex) {
                        verify(metricCollectorMock, times(1)).onGetFunctionsFailure(eq(userId), anyFloat(),
                                        eq(accountId),
                                        eq(500),
                                        any());
                        throw ex;
                }
        }

        @Test(expected = FaaSException.class)
        public void getFunctionsThrowFaaSExceptionWhenRuntimeExceptionOccurs() throws IOException, FaaSException {
                try {
                        when(restClientMock.get(eq(getExpectedFunctionsOfAnAccountUrl()), httpHeaderCaptor.capture(),
                                        eq(defaultTimeOut)))
                                        .thenThrow(new NullPointerException());
                        client.getFunctions(userId, new HashMap<String, String>(), optionalParams);
                } catch (Exception ex) {
                        verify(metricCollectorMock, times(1)).onGetFunctionsFailure(eq(userId), anyFloat(),
                                        eq(accountId),
                                        eq(-1),
                                        any());
                        throw ex;
                }
        }

        @Test
        public void isImplementedEventRetrievedFromCache() throws Exception {
                FaaSEventImplementedExpiry eventExpiry = new FaaSEventImplementedExpiry();
                eventExpiry.setImplemented(true);
                eventExpiry.setExpirationDate(LocalDateTime.now().plusMinutes(2));

                when(restClientMock.get(eq(getExpectedIsImplementedUrl()), httpHeaderCaptor.capture(),
                                eq(defaultTimeOut))).thenReturn(
                                                "{\"implemented\": true}");
                when(defaultIsImplementedCacheMock.getIfCachedAndValid(eq(event.toString()))).thenReturn(eventExpiry);

                boolean isImplemented = client.isImplemented(lpEventSource, event, optionalParams);

                verify(metricCollectorMock, times(0)).onIsImplementedSuccess(eq(lpEventSource), anyFloat(),
                                eq(event.toString()), eq(accountId));
                assertTrue("Lambda should be implemented", isImplemented);
                verify(restClientMock, times(0)).get(any(), any(), eq(optionalParams.getTimeOutInMs()));

        }

        @Test
        public void isImplementedStringEventRetrievedFromCache() throws Exception {
                String noDefinedEvent = "some-event";
                FaaSEventImplementedExpiry eventExpiry = new FaaSEventImplementedExpiry();
                eventExpiry.setImplemented(true);
                eventExpiry.setExpirationDate(LocalDateTime.now().plusMinutes(2));

                when(restClientMock.get(eq(getExpectedIsImplementedUrl()), httpHeaderCaptor.capture(),
                                eq(defaultTimeOut))).thenReturn(
                                                "{\"implemented\": true}");
                when(defaultIsImplementedCacheMock.getIfCachedAndValid(eq(noDefinedEvent.toString())))
                                .thenReturn(eventExpiry);

                boolean isImplemented = client.isImplemented(lpEventSource, noDefinedEvent, optionalParams);

                verify(metricCollectorMock, times(0)).onIsImplementedSuccess(eq(lpEventSource), anyFloat(),
                                eq(event.toString()), eq(accountId));
                assertTrue("Lambda should be implemented", isImplemented);
                verify(restClientMock, times(0)).get(any(), any(), eq(optionalParams.getTimeOutInMs()));

        }

        @Test
        public void isImplementedEventNoCacheWithDPoP() throws Exception {
                when(restClientMock.get(eq(getExpectedIsImplementedUrl()), httpHeaderCaptor.capture(),
                                eq(defaultTimeOut))).thenReturn(
                                                "{\"implemented\": true}");
                when(defaultIsImplementedCacheMock.getIfCachedAndValid(eq(event.toString()))).thenReturn(null);

                boolean isImplemented = clientWithDPoP.isImplemented(lpEventSource, event, optionalParams);

                assertTrue("Lambda should be implemented", isImplemented);
                verify(authDPoPSignatureBuilder, times(1)).getAccessTokenInternal(eq("https://" + faasGWUrl));
                verify(authDPoPSignatureBuilder, times(1)).getDpopHeaderInternal(
                                eq(getExpectedIsImplementedUrl()),
                                eq("GET"), eq(accessToken));
                assertTrue("Lambda invocation with wrong authorization header",
                                httpHeaderCaptor.getValue().get("Authorization").equals("DPoP " + accessToken));
                assertTrue("Lambda invocation with wrong DPoP header",
                                httpHeaderCaptor.getValue().get("DPoP").equals(dpopHeader));
                assertTrue(httpHeaderCaptor.getValue().get(
                                "LP-EventSource").contains(lpEventSource));

        }

        @Test(expected = FaaSDetailedExceptionV1.class)
        public void isImplementedThrowsFaaSDetailedException() throws IOException, FaaSException {
                try {
                        FaaSErrorV1 faaSError = new FaaSErrorV1("faas.error.code", "My custom error.");

                        when(restClientMock.get(eq(getExpectedIsImplementedUrl()), httpHeaderCaptor.capture(),
                                        eq(defaultTimeOut)))
                                        .thenThrow(new RestException("Error during rest call.",
                                                        objectMapper.writeValueAsString(faaSError),
                                                        500));

                        client.isImplemented(lpEventSource, event, optionalParams);
                } catch (Exception ex) {
                        verify(metricCollectorMock, times(1)).onIsImplementedFailure(eq(lpEventSource), anyFloat(),
                                        eq(event.toString()), eq(accountId), eq(500), any());
                        throw ex;
                }
        }

        @Test(expected = FaaSException.class)
        public void isImplementedThrowFaaSException() throws FaaSException, TokenGenerationException {
                try {
                        when(authSignatureBuilder.getAuthHeader())
                                        .thenThrow(new TokenGenerationException("could not generate " +
                                                        "token"));
                        client.isImplemented(lpEventSource, event, optionalParams);
                } catch (Exception ex) {
                        verify(metricCollectorMock, times(1)).onIsImplementedFailure(eq(lpEventSource), anyFloat(),
                                        eq(event.toString()), eq(accountId), eq(-1), any());
                        throw ex;
                }

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
                headers.put("LP-EventSource", lpEventSource);
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

        private String getExpectedFunctionsOfAnAccountUrl() {
                String expectedUrl = "https://%s/api/account/%s/functions?userId=%s";
                // String faasUIUrl = "faasUI.com";
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
                                "    \"name\": \"Sergey_Test\",\n" +
                                "    \"description\": \"Test\",\n" +
                                "    \"state\": \"Productive\",\n" +
                                "    \"skills\": [\n" +
                                "    ],\n" +
                                "    \"createdBy\": \"2851393312\",\n" +
                                "    \"updatedBy\": \"2851393312\",\n" +
                                "    \"createdAt\": \"2019-07-22T20:51:28.000Z\",\n" +
                                "    \"updatedAt\": \"2019-07-22T21:02:33.000Z\",\n" +
                                "    \"size\": \"S\",\n" +
                                "    \"isCompV1\": \"true\"\n" +
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
