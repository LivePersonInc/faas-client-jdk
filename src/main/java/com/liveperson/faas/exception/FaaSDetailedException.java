package com.liveperson.faas.exception;

import com.liveperson.faas.dto.FaaSErrorV1;

public class FaaSDetailedException extends FaaSException {
    FaaSErrorV1 faaSError;

    public FaaSDetailedException(FaaSErrorV1 faaSError, RestException cause){
        super(faaSError.toString(), cause);

        this.faaSError = faaSError;
    }

    @Override
    public synchronized RestException getCause() {
        return (RestException) super.getCause();
    }

    public FaaSErrorV1 getFaaSError() {
        return faaSError;
    }
}
