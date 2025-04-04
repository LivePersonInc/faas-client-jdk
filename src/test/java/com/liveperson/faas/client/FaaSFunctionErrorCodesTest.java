package com.liveperson.faas.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class FaaSFunctionErrorCodesTest {
    @Test
    public void containsShouldReturnTrueForCustomCode(){
        boolean contains = FaaSFunctionErrorCodes.contains("com.customer.faas.function.threw-error");

        assertTrue(contains);
    }

    @Test
    public void containsShouldNotReturnTrueForNoneCustomCode(){
        boolean contains = FaaSFunctionErrorCodes.contains("com.liveperson.faas.handler.none-custom-failure");

        assertFalse(contains);
    }
}
