package com.liveperson.faas.security;

import com.liveperson.faas.exception.TokenGenerationException;

/**
 * Retrieves the bearer token used for authentication purposes on other systems
 *
 * @author sschwarz
 */
public interface BearerGenerator {

    /**
     * @return bearer token used for authentication
     * @throws TokenGenerationException when token generation fails
     */
    String retrieveBearerToken() throws TokenGenerationException;

}


