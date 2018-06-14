package com.liveperson.faas.exception;

/**
 * FaaS specific exception type
 *
 * @author arotaru
 */
public class FaaSException extends Exception {
    public FaaSException(String message, Throwable cause) {
        super(message, cause);
    }

    public FaaSException(String message) {
        super(message);
    }
}
