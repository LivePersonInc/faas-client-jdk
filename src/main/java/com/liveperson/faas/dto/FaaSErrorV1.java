package com.liveperson.faas.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

/**
 *  @deprecated FaaSError will be used after transition to V2 Functions
 */
@JsonIgnoreProperties(value = {"errorLogs"})
public class FaaSErrorV1 {
    private String errorCode;
    private String errorMsg;

    public FaaSErrorV1() {
    }

    public FaaSErrorV1(String errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FaaSErrorV1 faaSError = (FaaSErrorV1) o;
        return Objects.equals(errorCode, faaSError.errorCode) &&
                Objects.equals(errorMsg, faaSError.errorMsg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(errorCode, errorMsg);
    }

    @Override
    public String toString() {
        return "FaaSError{" +
                "errorCode='" + errorCode + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                '}';
    }
}
