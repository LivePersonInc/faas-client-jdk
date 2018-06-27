package com.liveperson.faas.client;

import com.liveperson.csds.api.BaseURIData;
import com.liveperson.csds.client.CsdsWebClient;
import com.liveperson.csds.client.api.CsdsClient;
import com.liveperson.csds.client.api.CsdsWebClientConfig;
import com.liveperson.faas.dto.FaaSInvocation;
import com.liveperson.faas.exception.FaaSException;
import com.liveperson.faas.http.DefaultRestClient;
import com.liveperson.faas.http.RestClient;
import com.liveperson.faas.security.DefaultOAuthSignaturBuilder;
import com.liveperson.faas.security.OAuthSignaturBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.HashMap;
import java.util.Map;

/**
 * FaaS web client for invoking lambdas of a account/brand
 * over the eventsource gateway / asgard
 *
 * @author arotaru
 */
public class FaaSWebClient implements FaaSClient {

    private static final String CSDS_SERVICE_NAME = "faasGW";
    private static final String CSDS_DISK_STORAGE_PATH = "/tmp/";

    private static final int REST_CLIENT_CONNECTION_TIMOUT = 5000; //5 sec
    private static final int REST_CLIENT_READ_TIMOUT = 10000; //10 sec

    private static final String PROTOCOL = "http";
    private static final int DEFAULT_PORT = 80;
    private static final String QUERY_PARAM_USERID = "userId";
    private static final String QUERY_PARAM_APIVERSION = "v";
    private static final String API_VERSION = "1";
    private static final String INVOKE_URI = "api/account/%s/lambdas/%s/invoke";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private String gateway;
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
     * Factory method to create a instance of the FaaSClient
     * @param host the hostname of the eventsource gateway
     * @param port the port of the eventsource gateway (set -1 is not needed)
     * @param consumerKey the API key granted
     * @param consumerSecret the API secret granted
     * @return faas client instance
     * @throws FaaSException
     */
    public static FaaSClient getInstance(String host, int port, String consumerKey, String consumerSecret) {
        String gatewayUrl = port == DEFAULT_PORT ? host : String.format("%s:%d", host, port);
        RestClient restClient = new DefaultRestClient(REST_CLIENT_READ_TIMOUT, REST_CLIENT_CONNECTION_TIMOUT);
        OAuthSignaturBuilder oAuthSignaturBuilder = new DefaultOAuthSignaturBuilder(consumerKey, consumerSecret);
        return new FaaSWebClient(gatewayUrl, restClient, oAuthSignaturBuilder);
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

    /**
     * Constructor for direct invocation via host + port
     * @param gateway the eventsource gateway domain for the invocation
     * @param restClient the rest client instance
     */
    public FaaSWebClient(String gateway, RestClient restClient, OAuthSignaturBuilder oAuthSignaturBuilder) {
        this.gateway = gateway;
        this.restClient = restClient;
        this.oAuthSignaturBuilder = oAuthSignaturBuilder;
    }

    public <T> T invoke(String externalSystem, String accountId, String lambdaUUID, FaaSInvocation data, Class<T> responseType) throws FaaSException {
        try {
            // Create the complete url for invocation
            String invokeUri = String.format(this.INVOKE_URI, accountId, lambdaUUID);
            UriComponents uriComponents = UriComponentsBuilder.newInstance()
                    .scheme(PROTOCOL)
                    .host(this.getGatewayDomain(accountId))
                    .pathSegment(invokeUri)
                    .queryParam(QUERY_PARAM_USERID, externalSystem)
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
     * Determines the eventsource gateway domain for the RESTful call
     * @param accountId the brand/account id
     * @return the base url
     * @throws Exception
     */
    private String getGatewayDomain(String accountId) throws Exception {
        // Return from fix gateway
        if(this.gateway != null && !this.gateway.isEmpty()) return this.gateway;

        // Return from dynamic CSDS resolution
        BaseURIData baseURIData = csdsClient.get(accountId, CSDS_SERVICE_NAME);
        if (baseURIData == null) {
            throw new FaaSException("Could not resolve CSDS entry to faas domain.");
        }
        return baseURIData.getBaseURI();
    }
}
