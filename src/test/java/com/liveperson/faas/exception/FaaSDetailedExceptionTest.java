package com.liveperson.faas.exception;

import com.liveperson.faas.dto.FaaSError;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class FaaSDetailedExceptionTest {
    @Test
    public void getCauseShouldBeRestException(){
        //Set method mocks for mock objects
        FaaSError faaSError = new FaaSError("faas.error.code", "My custom error.");
        RestException restException = new RestException("Oops", "Oops", 500);

        FaaSDetailedException e = new FaaSDetailedException(faaSError, restException);

        assertEquals(faaSError, e.getFaaSError());
        assertEquals(e.getCause().getResponse(), restException.getResponse());
    }
}
