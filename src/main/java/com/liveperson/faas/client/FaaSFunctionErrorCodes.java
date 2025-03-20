package com.liveperson.faas.client;

/**
 * V2 Functions error Codes
 */
enum FaaSFunctionErrorCodes {
    CUSTOM_FAILURE("com.customer.faas.function.threw-error"),
    RUNTIME_EXCEPTION("com.customer.faas.function.js-runtime-error"),
    EXECUTION_WINDOW_EXCEEDED("com.customer.faas.function.execution-exceeded");

    // TODO: Check if other errors are required
    // https://gitlab.com/l1905/conversational-cloud-engineering/platform-enablement/functions/core-gcpevg/-/blob/develop/src/controllers/invocation.ts?ref_type=heads

    private String code;

    FaaSFunctionErrorCodes(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static boolean contains(String test) {
        for (FaaSFunctionErrorCodes c : FaaSFunctionErrorCodes.values()) {
            if (c.getCode().equals(test)) {
                return true;
            }
        }

        return false;
    }
}
