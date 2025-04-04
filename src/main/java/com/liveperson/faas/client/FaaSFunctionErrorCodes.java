package com.liveperson.faas.client;

import java.util.Arrays;

/**
 * V2 Functions error Codes
 */
public enum FaaSFunctionErrorCodes {
    CUSTOM_FAILURE("com.customer.faas.function.threw-error"),
    RUNTIME_EXCEPTION("com.customer.faas.function.js-runtime-error"),
    EXECUTION_WINDOW_EXCEEDED("com.customer.faas.function.execution-exceeded");

    private String code;

    FaaSFunctionErrorCodes(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static boolean contains(String test) {
        return Arrays.stream(FaaSFunctionErrorCodes.values())
                .map(FaaSFunctionErrorCodes::getCode)
                .anyMatch(c -> c.equals(test));
    }
}
