package com.liveperson.faas.security;

import com.liveperson.faas.exception.TokenGenerationException;

/**
 * Generates a header for authentication purposes on other systems
 *
 * @author sschwarz
 */
public interface AuthSignatureBuilder {

    /**
     * Generate authorization header string
     *
     * @return the authorization header
     */
    String getAuthHeader() throws TokenGenerationException;

}
