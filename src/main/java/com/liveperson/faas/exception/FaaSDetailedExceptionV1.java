package com.liveperson.faas.exception;

import com.liveperson.faas.dto.FaaSErrorV1;
/**
 *  @deprecated FaaSDetailedException will be used after transition to Functions V2
 */
public class FaaSDetailedExceptionV1 extends FaaSException {
    FaaSErrorV1 faaSError;

    public FaaSDetailedExceptionV1(FaaSErrorV1 faaSError, RestException cause){
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
