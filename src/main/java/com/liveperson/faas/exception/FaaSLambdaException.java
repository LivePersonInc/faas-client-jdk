package com.liveperson.faas.exception;

import com.liveperson.faas.dto.FaaSErrorV1;

public class FaaSLambdaException extends FaaSDetailedException {
    public FaaSLambdaException(FaaSErrorV1 faaSError, RestException cause) {
        super(faaSError, cause);
    }
}
