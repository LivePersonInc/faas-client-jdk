package com.liveperson.faas.security;

import com.liveperson.faas.exception.TokenGenerationException;
import com.liveperson.faas.exception.DPoPJwtGenerationException;

/**
 * Generates a Oauth2 + DPoP for authentication purposes
 * OAuth2+DPoP authentication is only available INTERNALLY for service-to-service.
 *
 * @author eplazaso
 */
public interface AuthDPoPSignatureBuilder {

    /**
     * Generate Oauth2 access token string. Called whenever the faas-client needs to authenticate to send a request.
     * Its return value is used in the 'Authorization'-header of the request.
     * 
     * @param domainUrl Protocol (HTTPS) + domain of the API registered in the authentication server required to get the access token. E.g., https://va.faasgw.liveperson.net
     * @throws TokenGenerationException when token generation fails
     * @return a string containing the access token that will be included in the 'Authorization' header
     */
    String getAccessTokenInternal(String domainUrl) throws TokenGenerationException;

    /**
     * Generate the DPoP header. Called whenever the faas-client needs to authenticate to send a request.
     * 
     * @param url Request 'url' including protocol domain and path
     * @param method 'http-method' of the request
     * @param accessToken A string containing the access token that was returned by 'getAccessTokenInternal' method
     * @return The the 'DPoP' header of the request.
     * @throws DPoPJwtGenerationException  when DPoP header generation fails
     */
    String getDpopHeaderInternal(String url, String method, String accessToken) throws DPoPJwtGenerationException;

}
