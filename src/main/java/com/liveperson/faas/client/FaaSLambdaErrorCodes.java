package com.liveperson.faas.client;

import java.util.Arrays;

/**
 *  V1 Functions error codes
 *  @deprecated FaaSFunctionErrorCodes will be used after transition to Functions V2
 */
public enum FaaSLambdaErrorCodes {
    CUSTOM_FAILURE("com.liveperson.faas.handler.custom-failure"),
    RUNTIME_EXCEPTION("com.liveperson.faas.handler.runtime-exception"),
    EXECUTION_WINDOW_EXCEEDED("com.liveperson.faas.handler.executiontime-exceeded"),
    LOG_LIMIT_REACHED("com.liveperson.faas.handler.log-limit-reached");

    private String code;

    FaaSLambdaErrorCodes(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static boolean contains(String test) {
        return Arrays.stream(FaaSLambdaErrorCodes.values())
                .map(FaaSLambdaErrorCodes::getCode)
                .anyMatch(c -> c.equals(test));
    }
}
