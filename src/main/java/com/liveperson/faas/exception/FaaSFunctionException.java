package com.liveperson.faas.exception;

import com.liveperson.faas.dto.FaaSError;

public class FaaSFunctionException extends FaaSDetailedException {
    public FaaSFunctionException(FaaSError faaSError, RestException cause) {
        super(faaSError, cause);
    }
}
