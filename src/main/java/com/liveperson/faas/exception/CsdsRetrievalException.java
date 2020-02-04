package com.liveperson.faas.exception;

public class CsdsRetrievalException extends Exception {
    public CsdsRetrievalException(String errorMessage) {
        super(errorMessage);
    }

    public CsdsRetrievalException(String errorMessage, Throwable e) {
        super(errorMessage, e);
    }
}
