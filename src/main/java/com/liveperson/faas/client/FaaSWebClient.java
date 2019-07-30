package com.liveperson.faas.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liveperson.csds.api.BaseURIData;
import com.liveperson.csds.client.CsdsWebClient;
import com.liveperson.csds.client.api.CsdsClient;
import com.liveperson.csds.client.api.CsdsWebClientConfig;
import com.liveperson.faas.dto.FaaSEventImplemented;
import com.liveperson.faas.dto.FaaSInvocation;
import com.liveperson.faas.exception.FaaSException;
import com.liveperson.faas.http.DefaultRestClient;
import com.liveperson.faas.http.RestClient;
import com.liveperson.faas.response.lambda.LambdaResponse;
import com.liveperson.faas.security.DefaultOAuthSignaturBuilder;
import com.liveperson.faas.security.OAuthSignaturBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FaaS web client for invoking lambdas of a account/brand
 * over the eventsource gateway / asgard
 *
 * @author arotaru
 */
public class FaaSWebClient implements FaaSClient {

    public static final String CSDS_EVG_SERVICE_NAME = "faasGW";
    public static final String CSDS_GW_SERVICE_NAME = "faasUI";
    private static final String CSDS_DISK_STORAGE_PATH = "/tmp/";

    private static final int REST_CLIENT_CONNECTION_TIMOUT = 5000; //5 sec
    private static final int REST_CLIENT_READ_TIMOUT = 10000; //10 sec

    private static final String PROTOCOL = "https";
    private static final String QUERY_PARAM_EXTERNAL_SYSTEM = "externalSystem";
    private static final String QUERY_PARAM_APIVERSION = "v";
    private static final String QUERY_PARAM_USER_ID = "userId";
    private static final String API_VERSION = "1";
    private static final String INVOKE_UUID_URI = "api/account/%s/lambdas/%s/invoke";
    private static final String INVOKE_EVENT_URI = "api/account/%s/events/%s/invoke";
    private static final String IS_IMPLEMENTED_URI = "api/account/%s/events/%s/isImplemented";
    private static final String GET_LAMBDAS_URI = "api/account/%s/lambdas";

    public static final String QUERY_PARAM_STATE = "state";
    public static final String QUERY_PARAM_EVENT_ID = "eventId";
    public static final String QUERY_PARAM_NAME = "name";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private CsdsClient csdsClient;
    private RestClient restClient;
    private OAuthSignaturBuilder oAuthSignaturBuilder;

    /**
     * Factory method to create a instance of the FaaSClient
     * @param csdsDomain the domain of the csds server
     * @param consumerKey the API key granted
     * @param consumerSecret the API secret granted
     * @return faas client instance
     * @throws FaaSException
     */
    public static FaaSClient getInstance(String csdsDomain, String consumerKey, String consumerSecret) throws FaaSException{
        RestClient restClient = new DefaultRestClient(REST_CLIENT_READ_TIMOUT, REST_CLIENT_CONNECTION_TIMOUT);
        OAuthSignaturBuilder oAuthSignaturBuilder = new DefaultOAuthSignaturBuilder(consumerKey, consumerSecret);
        return new FaaSWebClient(FaaSWebClient.getCsdsClient(csdsDomain), restClient, oAuthSignaturBuilder);
    }

    /**
     * Factory method to create a instance of the FaaSClient
     * @param csdsClient the csds client instance
     * @param consumerKey the API key granted
     * @param consumerSecret the API secret granted
     * @return faas client instance
     * @throws FaaSException
     */
    public static FaaSClient getInstance(CsdsClient csdsClient, String consumerKey, String consumerSecret) throws FaaSException{
        RestClient restClient = new DefaultRestClient(REST_CLIENT_READ_TIMOUT, REST_CLIENT_CONNECTION_TIMOUT);
        OAuthSignaturBuilder oAuthSignaturBuilder = new DefaultOAuthSignaturBuilder(consumerKey, consumerSecret);
        return new FaaSWebClient(csdsClient, restClient, oAuthSignaturBuilder);
    }

    /**
     * Constructor for invocation via CSDS domain resolution of Eventsource Gatewaay
     * @param csdsClient csds client used to the csds resolution
     * @param restClient the rest client instance
     * @throws FaaSException
     */
    public FaaSWebClient(CsdsClient csdsClient, RestClient restClient, OAuthSignaturBuilder oAuthSignaturBuilder) throws FaaSException {
        this.csdsClient = csdsClient;
        this.restClient = restClient;
        this.oAuthSignaturBuilder = oAuthSignaturBuilder;
    }

    public <T> T invoke(String externalSystem, String accountId, String lambdaUUID, FaaSInvocation data, Class<T> responseType) throws FaaSException {
        try {
            // Create the complete url for invocation
            String invokeUri = String.format(this.INVOKE_UUID_URI, accountId, lambdaUUID);
            UriComponents uriComponents = UriComponentsBuilder.newInstance()
                    .scheme(PROTOCOL)
                    .host(this.getEVGDomain(accountId))
                    .pathSegment(invokeUri)
                    .queryParam(QUERY_PARAM_EXTERNAL_SYSTEM, externalSystem)
                    .queryParam(QUERY_PARAM_APIVERSION, API_VERSION).build();


            String url = uriComponents.toUriString();

            // Generate the oAuth authorization header
            String authHeader = oAuthSignaturBuilder.getAuthHeader(HttpMethod.POST, url);

            // Execute the lambda invocation
            String response = restClient.post(url, this.setHeaders(authHeader), data.toString());

            // Parse and return lambda result
            return objectMapper.readValue(response, responseType);
        } catch(Exception e ){
            throw new FaaSException("Error occured during lambda invocation", e);
        }
    }

    public <T> T invoke(String externalSystem, String accountId, FaaSEvent event, FaaSInvocation data, Class<T> responseType) throws FaaSException {
        try {
            // Create the complete url for invocation
            String invokeUri = String.format(this.INVOKE_EVENT_URI, accountId, event);
            UriComponents uriComponents = UriComponentsBuilder.newInstance()
                    .scheme(PROTOCOL)
                    .host(this.getEVGDomain(accountId))
                    .pathSegment(invokeUri)
                    .queryParam(QUERY_PARAM_EXTERNAL_SYSTEM, externalSystem)
                    .queryParam(QUERY_PARAM_APIVERSION, API_VERSION).build();


            String url = uriComponents.toUriString();

            // Generate the oAuth authorization header
            String authHeader = oAuthSignaturBuilder.getAuthHeader(HttpMethod.POST, url);

            // Execute the lambda invocation
            String response = restClient.post(url, this.setHeaders(authHeader), data.toString());

            // Parse and return lambda result
            return objectMapper.readValue(response, responseType);
        } catch(Exception e ){
            throw new FaaSException("Error occured during lambda invocation", e);
        }
    }

    public boolean isImplemented(String externalSystem, String accountId, FaaSEvent event) throws FaaSException {
        try {
            // Create the complete url for invocation
            String invokeUri = String.format(this.IS_IMPLEMENTED_URI, accountId, event);
            UriComponents uriComponents = UriComponentsBuilder.newInstance()
                    .scheme(PROTOCOL)
                    .host(this.getEVGDomain(accountId))
                    .pathSegment(invokeUri)
                    .queryParam(QUERY_PARAM_EXTERNAL_SYSTEM, externalSystem)
                    .queryParam(QUERY_PARAM_APIVERSION, API_VERSION).build();

            String url = uriComponents.toUriString();

            // Generate the oAuth authorization header
            String authHeader = oAuthSignaturBuilder.getAuthHeader(HttpMethod.GET, url);

            // Execute the lambda invocation
            String response = restClient.get(url, this.setHeaders(authHeader));

            // Parse and return is implemented status
            return objectMapper.readValue(response, FaaSEventImplemented.class).getImplemented();
        } catch(Exception e ){
            throw new FaaSException("Error occured during check if lambda is implemented.", e);
        }
    }

    public List<LambdaResponse> getLambdasOfAccount(String accountId, String userId, Map<String, String> optionalQueryParams) throws FaaSException {
        try {
            // Create the complete url for invocation
            String getLambdasUri = String.format(GET_LAMBDAS_URI, accountId);
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance()
                    .scheme(PROTOCOL)
                    .host(getGWDomain(accountId))
                    .pathSegment(getLambdasUri)
                    .queryParam(QUERY_PARAM_APIVERSION, API_VERSION)
                    .queryParam(QUERY_PARAM_USER_ID, userId);

            if(!CollectionUtils.isEmpty(optionalQueryParams)) {
                updateQueryParams(QUERY_PARAM_STATE, optionalQueryParams, uriComponentsBuilder);
                updateQueryParams(QUERY_PARAM_EVENT_ID, optionalQueryParams, uriComponentsBuilder);
                updateQueryParams(QUERY_PARAM_NAME, optionalQueryParams, uriComponentsBuilder);
            }

            String url = uriComponentsBuilder.build().toUriString();

            // Generate the oAuth authorization header
            String authHeader = oAuthSignaturBuilder.getAuthHeader(HttpMethod.GET, url);

            // Get lambdas
            String response = restClient.get(url, setHeaders(authHeader));

            // Parse and return lambda result
            return objectMapper.readValue(response, new TypeReference<List<LambdaResponse>>() { });
        } catch(Exception e ){
            throw new FaaSException("Error occurred during lambdas fetch for account: " + accountId, e);
        }
    }

    /**
     * Initialize the CSDS client
     * @param csdsDomain the relevant csds domain
     * @return the csds client instance
     * @throws FaaSException
     */
    private static CsdsClient getCsdsClient(String csdsDomain) throws FaaSException {
        try {
            CsdsWebClient csdsClient = CsdsWebClient.getInstance();
            if (!csdsClient.isClientInitialized()) {
                CsdsWebClientConfig config = new CsdsWebClientConfig(csdsDomain, CSDS_DISK_STORAGE_PATH);
                csdsClient.init(config);
            }
            return csdsClient;
        } catch(Exception e){
            throw new FaaSException("Could not get CSDS client for FaaS client", e);
        }
    }

    /**
     * Set headers for the RESTful call
     * @param authorizationHeader the authorization header
     * @return the relevant headers
     */
    private Map<String, String> setHeaders(String authorizationHeader) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(HttpHeaders.AUTHORIZATION, authorizationHeader);
        return headers;
    }

    /**
     * Determines the eventsource gateway domain for the RESTful call.
     * This domain is used for invoking functions.
     * @param accountId the brand/account id
     * @return the base url
     * @throws Exception
     */
    private String getEVGDomain(String accountId) throws Exception {
        // Return from dynamic CSDS resolution
        BaseURIData baseURIData = csdsClient.get(accountId, CSDS_EVG_SERVICE_NAME);
        if (baseURIData == null) {
            throw new FaaSException("Could not resolve CSDS entry to faas domain.");
        }
        return baseURIData.getBaseURI();
    }

    /**
     * Determines the faas gateway domain for the RESTful call.
     * This domain is used for managing & deploying functions.
     * @param accountId the brand/account id
     * @return the base url
     * @throws Exception
     */
    private String getGWDomain(String accountId) throws Exception {
        // Return from dynamic CSDS resolution
        BaseURIData baseURIData = csdsClient.get(accountId, CSDS_GW_SERVICE_NAME);
        if (baseURIData == null) {
            throw new FaaSException("Could not resolve CSDS entry to faas domain.");
        }
        return baseURIData.getBaseURI();
    }

    private static void updateQueryParams(String key, Map<String, String> newQueryParamsMap, UriComponentsBuilder uriComponentsBuilder){
        String value = newQueryParamsMap.get(key);
        if(value != null) {
            uriComponentsBuilder.queryParam(key, value);
        }
    }
}
