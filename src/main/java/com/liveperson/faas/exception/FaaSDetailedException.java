package com.liveperson.faas.exception;

import com.liveperson.faas.dto.FaaSError;

public class FaaSDetailedException extends FaaSException {
    FaaSError faaSError;

    public FaaSDetailedException(FaaSError faaSError, RestException cause){
        super(faaSError.toString(), cause);

        this.faaSError = faaSError;
    }

    @Override
    public synchronized RestException getCause() {
        return (RestException) super.getCause();
    }

    public FaaSError getFaaSError() {
        return faaSError;
    }
}
