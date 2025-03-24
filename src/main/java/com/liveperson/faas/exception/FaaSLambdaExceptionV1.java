package com.liveperson.faas.exception;

import com.liveperson.faas.dto.FaaSErrorV1;

public class FaaSLambdaExceptionV1 extends FaaSDetailedExceptionV1 {
    public FaaSLambdaExceptionV1(FaaSErrorV1 faaSError, RestException cause) {
        super(faaSError, cause);
    }
}
