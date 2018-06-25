package com.liveperson.faas.security;

import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

/**
 * Create a OAuth1.0a conform signature
 * Hint: No token/token-secret support
 */
public class DefaultOAuthSignaturBuilder implements OAuthSignaturBuilder{
    private static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
    private static final String OAUTH_NONCE = "oauth_nonce";
    private static final String OAUTH_SIGNATURE = "oauth_signature";
    private static final String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
    private static final String OAUTH_SIGNATURE_METHOD_VALUE = "HMAC-SHA1";
    private static final String OAUTH_TIMESTAMP = "oauth_timestamp";
    private static final String OAUTH_VERSION = "oauth_version";
    private static final String OAUTH_VERSION_VALUE = "1.0";

    private final String consumerKey;
    private final String consumerSecret;

    // Message Authentication Code (MAC) object
    private Mac mac;

    // oAuth parameters need to be alphabetical sorted!!!
    private SortedMap<String, String> oAuthParameters;

    /**
     * Constructor
     * @param consumerKey the API key granted
     * @param consumerSecret the API secret granted
     */
    public DefaultOAuthSignaturBuilder(String consumerKey, String consumerSecret) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.oAuthParameters = new TreeMap<>();

        // Initialize mac
        String signingKey = encodeURL(consumerSecret) + "&";

        SecretKeySpec keySpec = new SecretKeySpec(signingKey.getBytes(), "HmacSHA1");
        try {
            this.mac = Mac.getInstance("HmacSHA1");
            this.mac.init(keySpec);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public String getAuthHeader(HttpMethod method, String url){
        String consumerKeyValue = encodeURL(consumerKey);
        String oauthNonce = getOauthNonce();
        String oauthTimestamp = String.valueOf(System.currentTimeMillis());

        // Set query parameters to oauth parameters
        processQueryParameters(url);

        // Add further oAuth parameters
        oAuthParameters.put(OAUTH_CONSUMER_KEY, consumerKeyValue);
        oAuthParameters.put(OAUTH_NONCE, oauthNonce);
        oAuthParameters.put(OAUTH_TIMESTAMP, oauthTimestamp);
        oAuthParameters.put(OAUTH_SIGNATURE_METHOD, OAUTH_SIGNATURE_METHOD_VALUE);
        oAuthParameters.put(OAUTH_VERSION, OAUTH_VERSION_VALUE);

        // Generate base string
        String baseString = method.name();
        String urlWithoutQueryParams = encodeURL(url.split("\\?")[0]);
        baseString += "&" + urlWithoutQueryParams + "&";

        boolean firstEntry = true;
        for (Map.Entry<String, String> entry : oAuthParameters.entrySet()) {
            if(!firstEntry) {
                baseString += encodeURL("&");
            }
            firstEntry = false;
            baseString += encodeURL(entry.getKey());
            baseString += encodeURL("=");
            baseString += encodeURL(entry.getValue());
        }

        // Merge all parameters together into the authorization header
        return "OAuth "
                + OAUTH_CONSUMER_KEY + "=\"" + consumerKeyValue + "\","
                + OAUTH_NONCE + "=\"" + oauthNonce + "\", "
                + OAUTH_SIGNATURE + "=\"" + getOauthSignature(baseString) + "\","
                + OAUTH_SIGNATURE_METHOD + "=\"" + OAUTH_SIGNATURE_METHOD_VALUE + "\","
                + OAUTH_TIMESTAMP + "=\"" + oauthTimestamp + "\","
                + OAUTH_VERSION + "=\"" + OAUTH_VERSION_VALUE + "\"";
    }

    /**
     * Generate nonce to ensure the signature cannot be used multiple times.
     * @return generated nonce
     */
    private String getOauthNonce(){
        // Create random bytes
        byte[] nonce = new byte[32];
        new SecureRandom().nextBytes(nonce);

        // Encode base64
        String oauthNonce = DatatypeConverter.printBase64Binary(nonce);

        // Remove all special characters
        return oauthNonce.replaceAll("\\W", "");
    }

    /**
     * Helper to do the url encoding for all relevant components
     * @param component
     * @return
     */
    private String encodeURL(String component){
        try {
            return URLEncoder.encode(component, StandardCharsets.UTF_8.displayName());
        } catch(Exception e){
            return "";
        }
    }

    /**
     * Encrypt the baseString via the MAC
     * @param baseString
     * @return encrypted signature
     */
    private String getOauthSignature(String baseString){
        byte[] result = this.mac.doFinal(baseString.getBytes());
        return encodeURL(DatatypeConverter.printBase64Binary(result));
    }

    /**
     * Extract the query parameters from the url and store them in the oAuth parameters
     * @param url the url containing the query params
     */
    private void processQueryParameters(String url){
        UriComponents builder = UriComponentsBuilder.fromUriString(url).build();
        MultiValueMap<String, String> parameters = builder.getQueryParams();
        Set<String> keys = parameters.keySet();

        for (String key : keys) {
            // Respect only the first query parameter with the same name
            oAuthParameters.put(key, parameters.getFirst(key));
        }
    }
}