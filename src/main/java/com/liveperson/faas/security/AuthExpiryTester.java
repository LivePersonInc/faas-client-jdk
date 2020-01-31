package com.liveperson.faas.security;

/**
 * Examines whether a token is expired or about to expire
 *
 * @author sschwarz
 */
public interface AuthExpiryTester {

    /**
     * @param token that is checked for expiry
     * @return true if token is expired, else false
     */
    boolean isExpired(String token);

    /**
     * @param token that is checked for expiry in next 30 minutes
     * @return true if token is about to expire, else false
     */
    boolean isAboutToExpire(String token);

}
