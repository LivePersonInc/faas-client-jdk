package com.liveperson.faas.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liveperson.faas.client.types.FaaSEventImplementedExpiry;
import com.liveperson.faas.client.types.OptionalParams;
import com.liveperson.faas.csds.CsdsClient;
import com.liveperson.faas.csds.CsdsMapClient;
import com.liveperson.faas.csds.CsdsWebClient;
import com.liveperson.faas.dto.FaaSError;
import com.liveperson.faas.dto.FaaSInvocation;
import com.liveperson.faas.exception.*;
import com.liveperson.faas.http.DefaultRestClient;
import com.liveperson.faas.http.RestClient;
import com.liveperson.faas.metriccollector.MetricCollector;
import com.liveperson.faas.metriccollector.NullMetricCollector;
import com.liveperson.faas.response.lambda.LambdaResponse;
import com.liveperson.faas.security.AuthDPoPSignatureBuilder;
import com.liveperson.faas.security.AuthSignatureBuilder;
import com.liveperson.faas.security.JwtSignatureBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * FaaS web client for invoking lambdas of a account/brand
 * over the eventsource gateway / asgard
 *
 * @author sschwarz
 * @author arotaru
 */
public class FaaSWebClient implements FaaSClient {

    public static final String CSDS_GW_SERVICE_NAME = "faasGW";
    public static final String CSDS_UI_SERVICE_NAME = "faasUI";
    public static final String QUERY_PARAM_STATE = "state";
    public static final String QUERY_PARAM_EVENT_ID = "eventId";
    public static final String QUERY_PARAM_NAME = "name";
    private static final String PROTOCOL = "https";
    private static final String QUERY_PARAM_EXTERNAL_SYSTEM = "externalSystem";
    private static final String QUERY_PARAM_APIVERSION = "v";
    private static final String QUERY_PARAM_USER_ID = "userId";
    private static final String API_VERSION = "1";
    private static final String INVOKE_UUID_URI = "api/account/%s/lambdas/%s/invoke";
    private static final String INVOKE_EVENT_URI = "api/account/%s/events/%s/invoke";
    private static final String IS_IMPLEMENTED_URI = "api/account/%s/events/%s/isImplemented";
    private static final String GET_LAMBDAS_URI = "api/account/%s/lambdas";
    private static final String REQUEST_LOG_INVOKE = "Invocation with requestID %s will be carried out for accountID " +
            "%s with " +
            "url %s and requestBody %s";
    private static final String REQUEST_LOG_IS_IMPLEMENTED = "Is Implemented request with requestID %s will be " +
            "carried out for accountId %s with url %s";
    private static final String REQUEST_LOG_GET_LAMBDAS = "Get Lambdas request with requestID %s will be " +
            "carried out for accountId %s with url %s";
    private static final String REQUEST_REST_EXCEPTION_LOG = "Rest exception occurred for request to url %s with " +
            "requestID %s" +
            " and " +
            "accountID %s. Status code was %s with error message %s";
    private static final String REQUEST_EXCEPTION_LOG = "Exception occurred for request to url %s with requestID" +
            " %s" +
            " and " +
            "accountID %s. Error message was %s";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    static Logger logger = LogManager.getLogger();
    private CsdsClient csdsClient;
    private RestClient restClient;
    private AuthSignatureBuilder authSignatureBuilder;
    private AuthDPoPSignatureBuilder authDPoPSignatureBuilder;
    private MetricCollector metricCollector;
    private String accountId;
    private IsImplementedCache isImplementedCache;

    private FaaSWebClient() {
    }

    private static void updateQueryParams(String key, Map<String, String> newQueryParamsMap,
            UriComponentsBuilder uriComponentsBuilder) {
        String value = newQueryParamsMap.get(key);
        if (value != null) {
            uriComponentsBuilder.queryParam(key, value);
        }
    }

    @Override
    public <T> T invokeByUUID(String externalSystem, String lambdaUUID, FaaSInvocation data, Class<T> responseType,
            OptionalParams optionalParams) throws FaaSException {
        String invokeUri = String.format(FaaSWebClient.INVOKE_UUID_URI, accountId, lambdaUUID);
        return invokeWithUri(externalSystem, data, responseType, invokeUri, optionalParams);
    }

    @Override
    public void invokeByUUID(String externalSystem, String lambdaUUID, FaaSInvocation data,
            OptionalParams optionalParams) throws FaaSException {
        String invokeUri = String.format(FaaSWebClient.INVOKE_UUID_URI, accountId, lambdaUUID);
        invokeWithUriNoResponse(externalSystem, data, invokeUri, optionalParams);

    }

    @Override
    public <T> T invokeByEvent(String externalSystem, FaaSEvent event, FaaSInvocation data, Class<T> responseType,
            OptionalParams optionalParams) throws FaaSException {
        String invokeUri = String.format(FaaSWebClient.INVOKE_EVENT_URI, accountId, event);
        return invokeWithUri(externalSystem, data, responseType, invokeUri, optionalParams);
    }

    @Override
    public <T> T invokeByEvent(String externalSystem, String event, FaaSInvocation data, Class<T> responseType,
            OptionalParams optionalParams) throws FaaSException {
        String invokeUri = String.format(FaaSWebClient.INVOKE_EVENT_URI, accountId, event);
        return invokeWithUri(externalSystem, data, responseType, invokeUri, optionalParams);
    }

    @Override
    public void invokeByEvent(String externalSystem, FaaSEvent event, FaaSInvocation data,
            OptionalParams optionalParams) throws FaaSException {
        String invokeUri = String.format(FaaSWebClient.INVOKE_EVENT_URI, accountId, event);
        invokeWithUriNoResponse(externalSystem, data, invokeUri, optionalParams);
    }

    @Override
    public void invokeByEvent(String externalSystem, String event, FaaSInvocation data,
            OptionalParams optionalParams) throws FaaSException {
        String invokeUri = String.format(FaaSWebClient.INVOKE_EVENT_URI, accountId, event);
        invokeWithUriNoResponse(externalSystem, data, invokeUri, optionalParams);
    }

    public boolean isImplemented(String externalSystem, FaaSEvent event, OptionalParams optionalParams)
            throws FaaSException {
        String eventId = event.toString();
        return isEventImplemented(externalSystem, eventId, optionalParams);
    }

    @Override
    public boolean isImplemented(String externalSystem, String event, OptionalParams optionalParams)
            throws FaaSException {
        return isEventImplemented(externalSystem, event, optionalParams);
    }

    private boolean isEventImplemented(String externalSystem, String event, OptionalParams optionalParams)
            throws FaaSException {
        String requestId = optionalParams.getRequestId().equals("") ? UUID.randomUUID().toString()
                : optionalParams.getRequestId();
        int timeOutInMs = optionalParams.getTimeOutInMs();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        FaaSEventImplementedExpiry eventExpiry = isImplementedCache.getIfCachedAndValid(event);
        String url = "unresolved";
        if (eventExpiry != null) {
            return eventExpiry.isImplemented();
        }
        try {
            String invokeUri = String.format(IS_IMPLEMENTED_URI, accountId, event);
            url = buildGWDomainUrl(externalSystem, invokeUri);
            logger.info(String.format(REQUEST_LOG_IS_IMPLEMENTED, requestId, accountId, url));

            Map<String, String> headers = generateRequestHeaders(this.getGWDomain(), url, requestId,
                    HttpMethod.GET.name());

            String response = restClient.get(url, headers, timeOutInMs);

            boolean isImplemented = objectMapper.readValue(response,
                    com.liveperson.faas.dto.FaaSEventImplemented.class).getImplemented();
            isImplementedCache.update(event, isImplemented);
            stopWatch.stop();
            metricCollector.onIsImplementedSuccess(externalSystem, stopWatch.getTotalTimeSeconds(), event,
                    accountId);

            return isImplemented;
        } catch (RestException e) {
            logger.error(String.format(REQUEST_REST_EXCEPTION_LOG, url, requestId, accountId, e.getStatusCode(),
                    e.getMessage()));
            FaaSError faaSError = getFaaSError(e);
            collectMetricsIsImplementedFails(externalSystem, event, stopWatch, e,
                    e.getStatusCode());
            throw new FaaSDetailedException(faaSError, e);
        } catch (Exception e) {
            logger.error(String.format(REQUEST_EXCEPTION_LOG, url, requestId, accountId,
                    e.getMessage()));
            collectMetricsIsImplementedFails(externalSystem, event, stopWatch, e, -1);
            throw new FaaSException("Error occured during check if lambda is implemented.", e);
        }
    }

    private void collectMetricsIsImplementedFails(String externalSystem, String eventId, StopWatch stopWatch,
            Exception e, int statusCode) {
        if (stopWatch.isRunning())
            stopWatch.stop();
        metricCollector.onIsImplementedFailure(externalSystem, stopWatch.getTotalTimeSeconds(), eventId, accountId,
                statusCode, e);
    }

    public List<LambdaResponse> getLambdas(String userId, Map<String, String> optionalQueryParams,
            OptionalParams optionalParams) throws FaaSException {
        String requestId = optionalParams.getRequestId().equals("") ? UUID.randomUUID().toString()
                : optionalParams.getRequestId();
        int timeOutInMs = optionalParams.getTimeOutInMs();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String url = "unresolved";
        try {
            String getLambdasUri = String.format(GET_LAMBDAS_URI, accountId);
            url = buildUIDomainUrl(userId, optionalQueryParams, getLambdasUri);

            Map<String, String> headers = generateRequestHeaders(this.getUIDomain(), url, requestId,
                    HttpMethod.GET.name());

            logger.info(String.format(REQUEST_LOG_GET_LAMBDAS, requestId, accountId, url));
            String response = restClient.get(url, headers, timeOutInMs);
            stopWatch.stop();
            metricCollector.onGetLambdasSuccess(userId, stopWatch.getTotalTimeSeconds(), accountId);
            return objectMapper.readValue(response, new TypeReference<List<LambdaResponse>>() {
            });
        } catch (RestException e) {
            logger.error(String.format(REQUEST_REST_EXCEPTION_LOG, url, requestId, accountId, e.getStatusCode(),
                    e.getMessage()));
            collectMetricsGetLambdasFails(userId, stopWatch, e.getStatusCode(), e);
            FaaSError faaSError = this.getFaaSError(e);
            throw new FaaSDetailedException(faaSError, e);
        } catch (Exception e) {
            logger.error(String.format(REQUEST_EXCEPTION_LOG, url, requestId, accountId,
                    e.getMessage()));
            collectMetricsGetLambdasFails(userId, stopWatch, -1, e);
            throw new FaaSException("Error occurred during lambdas fetch for account: " + accountId, e);
        }
    }

    private void collectMetricsGetLambdasFails(String userId, StopWatch stopWatch, int statusCode, Exception e) {
        if (stopWatch.isRunning())
            stopWatch.stop();
        metricCollector.onGetLambdasFailure(userId, stopWatch.getTotalTimeSeconds(), accountId, statusCode, e);
    }

    private <T> T invokeWithUri(String externalSystem, FaaSInvocation data,
            Class<T> responseType, String invokeUri, OptionalParams optionalParams) throws FaaSException {
        String requestId = optionalParams.getRequestId().equals("") ? UUID.randomUUID().toString()
                : optionalParams.getRequestId();
        int timeOutInMs = optionalParams.getTimeOutInMs();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        boolean isLambda = true;
        String lambdaOrEventName = "";
        String url = "unresolved";
        try {
            isLambda = invokeUri.contains("lambdas");
            lambdaOrEventName = extractLambdaOrEventName(invokeUri);
            url = buildGWDomainUrl(externalSystem, invokeUri);

            Map<String, String> headers = generateRequestHeaders(this.getGWDomain(), url, requestId,
                    HttpMethod.POST.name());

            logger.info(String.format(REQUEST_LOG_INVOKE, requestId, accountId, url, data));
            String response = restClient.post(url, headers, data.toString(),
                    timeOutInMs);
            collectMetricsForSuccessfulInvocation(externalSystem, stopWatch, isLambda, lambdaOrEventName);

            return objectMapper.readValue(response, responseType);
        } catch (RestException e) {
            logger.error(String.format(REQUEST_REST_EXCEPTION_LOG, url, requestId, accountId, e.getStatusCode(),
                    e.getMessage()));
            collectMetricsForFailedInvocation(externalSystem, stopWatch, isLambda, lambdaOrEventName, e,
                    e.getStatusCode());
            throw handleFaaSInvocationException(e);
        } catch (Exception e) {
            logger.error(String.format(REQUEST_EXCEPTION_LOG, url, requestId, accountId,
                    e.getMessage()));
            collectMetricsForFailedInvocation(externalSystem, stopWatch, isLambda, lambdaOrEventName, e, -1);
            throw new FaaSException("Error occured during lambda invocation", e);
        }
    }

    private String extractLambdaOrEventName(String invokeUri) {
        String[] invokeUriSplit = invokeUri.split("/");
        String lambdaOrEventName = invokeUriSplit[invokeUriSplit.length - 2];
        return lambdaOrEventName;
    }

    private void invokeWithUriNoResponse(String externalSystem, FaaSInvocation data, String invokeUri,
            OptionalParams optionalParams) throws FaaSException {
        String requestId = optionalParams.getRequestId().equals("") ? UUID.randomUUID().toString()
                : optionalParams.getRequestId();
        int timeOutInMs = optionalParams.getTimeOutInMs();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        boolean isLambda = true;
        String lambdaOrEventName = "";
        String url = "unresolved";
        try {
            isLambda = invokeUri.contains("lambdas");
            lambdaOrEventName = extractLambdaOrEventName(invokeUri);
            url = buildGWDomainUrl(externalSystem, invokeUri);

            Map<String, String> headers = generateRequestHeaders(this.getGWDomain(), url, requestId,
                    HttpMethod.POST.name());

            logger.info(String.format(REQUEST_LOG_INVOKE, requestId, accountId, url, data));
            restClient.post(url, headers, data.toString(), timeOutInMs);
            collectMetricsForSuccessfulInvocation(externalSystem, stopWatch, isLambda, lambdaOrEventName);
        } catch (RestException e) {
            logger.error(String.format(REQUEST_REST_EXCEPTION_LOG, url, requestId, accountId, e.getStatusCode(),
                    e.getMessage()));
            collectMetricsForFailedInvocation(externalSystem, stopWatch, isLambda, lambdaOrEventName, e,
                    e.getStatusCode());
            throw handleFaaSInvocationException(e);
        } catch (Exception e) {
            logger.error(String.format(REQUEST_EXCEPTION_LOG, url, requestId, accountId,
                    e.getMessage()));
            collectMetricsForFailedInvocation(externalSystem, stopWatch, isLambda, lambdaOrEventName, e, -1);
            throw new FaaSException("Error occured during lambda invocation", e);
        }
    }

    private void collectMetricsForSuccessfulInvocation(String externalSystem, StopWatch stopWatch, boolean isLambda,
            String lambdaOrEventName) {
        if (stopWatch.isRunning())
            stopWatch.stop();
        if (isLambda)
            metricCollector.onInvokeByUUIDSuccess(externalSystem, stopWatch.getTotalTimeSeconds(), lambdaOrEventName,
                    accountId);
        else
            metricCollector.onInvokeByEventSuccess(externalSystem, stopWatch.getTotalTimeSeconds(), lambdaOrEventName,
                    accountId);
    }

    private void collectMetricsForFailedInvocation(String externalSystem, StopWatch stopWatch, boolean isLambda,
            String lambdaOrEventName, Exception e, int statusCode) {
        if (stopWatch.isRunning())
            stopWatch.stop();
        if (isLambda)
            metricCollector.onInvokeByUUIDFailure(externalSystem, stopWatch.getTotalTimeSeconds(), lambdaOrEventName,
                    accountId, statusCode, e);
        else
            metricCollector.onInvokeByEventFailure(externalSystem, stopWatch.getTotalTimeSeconds(), lambdaOrEventName,
                    accountId, statusCode, e);
    }

    private String buildUIDomainUrl(String userId, Map<String, String> optionalQueryParams,
            String getLambdasUri) throws CsdsRetrievalException {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance()
                .scheme(PROTOCOL)
                .host(getUIDomain())
                .pathSegment(getLambdasUri)
                .queryParam(QUERY_PARAM_APIVERSION, API_VERSION)
                .queryParam(QUERY_PARAM_USER_ID, userId);

        if (!CollectionUtils.isEmpty(optionalQueryParams)) {
            updateQueryParams(QUERY_PARAM_STATE, optionalQueryParams, uriComponentsBuilder);
            updateQueryParams(QUERY_PARAM_EVENT_ID, optionalQueryParams, uriComponentsBuilder);
            updateQueryParams(QUERY_PARAM_NAME, optionalQueryParams, uriComponentsBuilder);
        }

        return uriComponentsBuilder.build().toUriString();
    }

    private String buildGWDomainUrl(String externalSystem, String invokeUri) throws Exception {
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme(PROTOCOL)
                .host(this.getGWDomain())
                .pathSegment(invokeUri)
                .queryParam(QUERY_PARAM_EXTERNAL_SYSTEM, externalSystem)
                .queryParam(QUERY_PARAM_APIVERSION, API_VERSION).build();

        return uriComponents.toUriString();
    }

    private FaaSException handleFaaSInvocationException(RestException e) throws FaaSException {
        FaaSError faaSError = this.getFaaSError(e);
        if (FaaSLambdaErrorCodes.contains(faaSError.getErrorCode()))
            throw new FaaSLambdaException(faaSError, e);

        throw new FaaSDetailedException(faaSError, e);
    }

    private FaaSError getFaaSError(RestException e) throws FaaSException {
        try {
            return objectMapper.readValue(e.getResponse(), FaaSError.class);
        } catch (IOException ex) {
            throw new FaaSException("Error occured during lambda invocation", ex);
        }
    }

    /**
     * Generates the request headers based on the authentication method
     * 
     * @param domain    the request domain
     * @param url       the request full url including domain and path parameters
     * @param requestId the request identification
     * @param method    the request method
     * @return the request required headers
     * @throws TokenGenerationException
     * @throws DPoPJwtGenerationException
     */
    private Map<String, String> generateRequestHeaders(String domain, String url, String requestId, String method)
            throws TokenGenerationException, DPoPJwtGenerationException {
        if (this.authDPoPSignatureBuilder != null) {
            String domainUrl = PROTOCOL + "://" + domain;
            String accessToken = authDPoPSignatureBuilder.getAccessTokenInternal(domainUrl);
            String dpopJwt = authDPoPSignatureBuilder.getDpopHeaderInternal(url, method, accessToken);
            return this.getHeaders(accessToken, requestId, dpopJwt);
        } else {
            String authHeader = authSignatureBuilder.getAuthHeader();
            return this.getHeaders(authHeader, requestId);
        }
    }

    /**
     * Set headers for the RESTful call
     *
     * @param authorizationHeader the authorization header
     * @param requestId           identifies the request
     * @return the relevant headers
     */
    private Map<String, String> getHeaders(String authorizationHeader, String requestId) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-REQUEST-ID", requestId);
        headers.put(HttpHeaders.AUTHORIZATION, authorizationHeader);
        return headers;
    }

    /**
     * Set headers for the RESTful call
     *
     * @param accessToken the Oauth2 + DPoP access token
     * @param requestId   identifies the request
     * @param dpopJwt     the DPoP header
     * @return the relevant headers
     */
    private Map<String, String> getHeaders(String accessToken, String requestId, String dpopJwt) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-REQUEST-ID", requestId);
        headers.put(HttpHeaders.AUTHORIZATION, "DPoP " + accessToken);
        headers.put("DPoP", dpopJwt);
        return headers;
    }

    /**
     * Determines the faas UI domain for the RESTful call.
     * This domain is used for managing & deploying functions.
     *
     * @return the base url
     * @throws CsdsRetrievalException
     */
    private String getUIDomain() throws CsdsRetrievalException {
        return csdsClient.getDomain(CSDS_UI_SERVICE_NAME);
    }

    /**
     * Determines the faas gateway domain for the RESTful call.
     * This domain is used for invoking functions.
     *
     * @return the base url
     * @throws CsdsRetrievalException
     */
    private String getGWDomain() throws CsdsRetrievalException {
        return csdsClient.getDomain(CSDS_GW_SERVICE_NAME);
    }

    public static class Builder {
        private CsdsClient csdsClient;
        private Map<String, String> csdsMap;
        private RestClient restClient;
        private AuthSignatureBuilder authSignatureBuilder;
        private AuthDPoPSignatureBuilder authDPoPSignatureBuilder;
        private MetricCollector metricCollector;
        private String accountId;
        private String clientSecret;
        private String clientId;
        private IsImplementedCache isImplementedCache;

        public Builder(String accountId) {
            this.accountId = accountId;
        }

        public Builder withCsdsClient(CsdsClient csdsClient) {
            this.csdsClient = csdsClient;
            return this;
        }

        public Builder withCsdsMap(Map<String, String> csdsMap) {
            this.csdsMap = csdsMap;
            return this;
        }

        public Builder withIsImplementedCache(IsImplementedCache isImplementedCache) {
            this.isImplementedCache = isImplementedCache;
            return this;
        }

        public Builder withRestClient(RestClient restClient) {
            this.restClient = restClient;
            return this;
        }

        public Builder withAuthSignatureBuilder(AuthSignatureBuilder authSignatureBuilder) {
            this.authSignatureBuilder = authSignatureBuilder;
            return this;
        }

        public Builder withAuthDPoPSignatureBuilder(AuthDPoPSignatureBuilder authDPoPSignatureBuilder) {
            this.authDPoPSignatureBuilder = authDPoPSignatureBuilder;
            return this;
        }

        public Builder withMetricCollector(MetricCollector metricCollector) {
            this.metricCollector = metricCollector;
            return this;
        }

        public Builder withClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public Builder withClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        private boolean isInitalized(Object o) {
            return o != null;
        }

        public FaaSWebClient build() {
            FaaSWebClient client = new FaaSWebClient();
            client.accountId = this.accountId;
            client.restClient = isInitalized(this.restClient) ? this.restClient : new DefaultRestClient();
            if (isInitalized(this.metricCollector)) {
                client.metricCollector = this.metricCollector;
            } else {
                client.metricCollector = new NullMetricCollector();
            }

            if (isInitalized(this.csdsClient)) {
                client.csdsClient = this.csdsClient;
            } else if (isInitalized(this.csdsMap)) {
                client.csdsClient = new CsdsMapClient(this.csdsMap);
            } else {
                client.csdsClient = new CsdsWebClient(client.restClient, accountId);
            }

            if (isInitalized(this.authSignatureBuilder)) {
                client.authSignatureBuilder = this.authSignatureBuilder;
            } else if (isInitalized(this.clientSecret) && isInitalized(this.clientId)) {
                client.authSignatureBuilder = new JwtSignatureBuilder(client.restClient, client.csdsClient, accountId,
                        this.clientId, this.clientSecret);
            } else if (isInitalized(this.authDPoPSignatureBuilder)) {
                client.authDPoPSignatureBuilder = this.authDPoPSignatureBuilder;
            } else {
                throw new IllegalStateException(
                        "Neither AuthSignatureBuilder instance, nor clientId and clientSecret, nor AuthDpopSignatureBuilder"
                                +
                                " were provided, thus impossible to use any authentication method");
            }
            if (isInitalized(this.isImplementedCache))
                client.isImplementedCache = this.isImplementedCache;
            else {
                client.isImplementedCache = new DefaultIsImplementedCache(60);
            }
            return client;
        }
    }
}