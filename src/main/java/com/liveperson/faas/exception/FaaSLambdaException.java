package com.liveperson.faas.exception;

import com.liveperson.faas.dto.FaaSErrorV1;

/**
 *  @deprecated FaaSFunctionException will be used after transition to Functions V2
 */
public class FaaSLambdaException extends FaaSDetailedExceptionV1 {
    public FaaSLambdaException(FaaSErrorV1 faaSError, RestException cause) {
        super(faaSError, cause);
    }
}
