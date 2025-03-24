package com.liveperson.faas.client;

public enum FaaSLambdaErrorCodesV1 {
    CUSTOM_FAILURE("com.liveperson.faas.handler.custom-failure"),
    RUNTIME_EXCEPTION("com.liveperson.faas.handler.runtime-exception"),
    EXECUTION_WINDOW_EXCEEDED("com.liveperson.faas.handler.executiontime-exceeded"),
    LOG_LIMIT_REACHED("com.liveperson.faas.handler.log-limit-reached");

    private String code;

    FaaSLambdaErrorCodesV1(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static boolean contains(String test) {
        for (FaaSLambdaErrorCodesV1 c : FaaSLambdaErrorCodesV1.values()) {
            if (c.getCode().equals(test)) {
                return true;
            }
        }

        return false;
    }
}
