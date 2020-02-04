package com.liveperson.faas.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class FaaSLambdaErrorCodesTest {
    @Test
    public void containsShouldReturnTrueForCustomCode(){
        boolean contains = FaaSLambdaErrorCodes.contains("com.liveperson.faas.handler.custom-failure");

        assertTrue(contains);
    }

    @Test
    public void containsShouldNotReturnTrueForNoneCustomCode(){
        boolean contains = FaaSLambdaErrorCodes.contains("com.liveperson.faas.handler.none-custom-failure");

        assertFalse(contains);
    }
}
