package com.liveperson.faas.client;

import com.liveperson.csds.api.BaseURIData;
import com.liveperson.csds.client.CsdsWebClient;
import com.liveperson.csds.client.api.CsdsClient;
import com.liveperson.csds.client.api.CsdsWebClientConfig;
import com.liveperson.faas.dto.FaaSInvocation;
import com.liveperson.faas.exception.FaaSException;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private static final String QUERY_PARAM_USERID = "userId";
    private static final String QUERY_PARAM_APIVERSION = "v";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String API_VERSION = "1";
    private static final String INVOKE_URI = "api/account/%s/lambdas/%s/invoke";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private String gateway;
    private CsdsClient csdsClient;
    private RestTemplate restTemplate;

    /**
     * Factory method to create a instance of the FaaSClient
     * @param csdsDomain the domain of the csds server
     * @return faas client instance
     * @throws FaaSException
     */
    public static FaaSClient getInstance(String csdsDomain) throws FaaSException{
        RestTemplate restTemplate = FaaSWebClient.createRestTemplate();
        return new FaaSWebClient(FaaSWebClient.getCsdsClient(csdsDomain), restTemplate);
    }

    /**
     * Factory method to create a instance of the FaaSClient
     * @param host the hostname of the eventsource gateway
     * @param port the port of the eventsource gateway
     * @return faas client instance
     * @throws FaaSException
     */
    public static FaaSClient getInstance(String host, int port) {
        String gatewayUrl = String.format("%s:%d", host, port);
        RestTemplate restTemplate = FaaSWebClient.createRestTemplate();
        return new FaaSWebClient(gatewayUrl, restTemplate);
    }

    /**
     * Constructor for invocation via CSDS domain resolution of Eventsource Gatewaay
     * @param csdsClient csds client used to the csds resolution
     * @param restTemplate the rest client instance
     * @throws FaaSException
     */
    public FaaSWebClient(CsdsClient csdsClient, RestTemplate restTemplate) throws FaaSException {
        this.csdsClient = csdsClient;
        this.restTemplate = restTemplate;
    }

    /**
     * Constructor for direct invocation via host + port
     * @param gateway the eventsource gateway domain for the invocation
     * @param restTemplate the rest client instance
     */
    public FaaSWebClient(String gateway, RestTemplate restTemplate) {
        this.gateway = gateway;
        this.restTemplate = restTemplate;
    }

    public <T> T invoke(String externalSystem, String authHeader, String accountId, String lambdaUUID, FaaSInvocation data, Class<T> responseType) throws FaaSException {
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

            // Execute the lambda invocation
            HttpEntity<String> request = new HttpEntity<String>(data.toString(), this.setHeaders(authHeader));
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            // Parse and return lambda result
            return objectMapper.readValue(response.getBody(), responseType);
        } catch(HttpClientErrorException e){
            throw new FaaSException("Received status code " + e.getStatusCode() + " and error " + e.getResponseBodyAsString(), e);
        } catch(Exception e ){
            throw new FaaSException("Error occured during lambda invocation", e);
        }
    }

    /**
     * Create a new rest client
     */
    private static RestTemplate createRestTemplate(){
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new SimpleClientHttpRequestFactory());
        SimpleClientHttpRequestFactory rf = (SimpleClientHttpRequestFactory) restTemplate
                .getRequestFactory();
        rf.setConnectTimeout(REST_CLIENT_CONNECTION_TIMOUT);
        rf.setReadTimeout(REST_CLIENT_READ_TIMOUT);
        return restTemplate;
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
    private HttpHeaders setHeaders(String authorizationHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HEADER_AUTHORIZATION, authorizationHeader);
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
