package com.liveperson.faas.exception;

import com.liveperson.faas.dto.FaaSError;

public class FaaSLambdaException extends FaaSDetailedException {
    public FaaSLambdaException(FaaSError faaSError, RestException cause) {
        super(faaSError, cause);
    }
}
