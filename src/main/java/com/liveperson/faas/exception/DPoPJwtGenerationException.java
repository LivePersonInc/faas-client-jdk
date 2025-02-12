package com.liveperson.faas.exception;

public class DPoPJwtGenerationException extends Exception {

    public DPoPJwtGenerationException(String message) {
        super(message);
    }

    public DPoPJwtGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
