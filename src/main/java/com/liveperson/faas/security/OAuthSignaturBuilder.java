package com.liveperson.faas.security;

import org.springframework.http.HttpMethod;

/**
 * Create a OAuth1.0a conform signature
 * Hint: No token/token-secret support
 * @Link https://oauth1.wp-api.org/docs/basics/Signing.html
 */
public interface OAuthSignaturBuilder {
    /**
     * Generate the oAuth1.0a authorization header string
     * @param method the http method
     * @param url the url to generate the header for
     * @return the authorization header
     */
    public String getAuthHeader(HttpMethod method, String url);
}
