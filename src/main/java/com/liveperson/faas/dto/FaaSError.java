package com.liveperson.faas.dto;
import java.util.Objects;

/**
 * V2 FaaS error
 */
public class FaaSError {
    private String code;
    private String message;

    public FaaSError() {
    }

    public FaaSError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getErrorCode() {
        return code;
    }

    public void setErrorCode(String errorCode) {
        this.code = errorCode;
    }

    public String getErrorMsg() {
        return message;
    }

    public void setErrorMsg(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FaaSError faaSError = (FaaSError) o;
        return Objects.equals(code, faaSError.code) &&
                Objects.equals(message, faaSError.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message);
    }

    @Override
    public String toString() {
        return "FaaSError{" +
                "errorCode='" + code + '\'' +
                ", errorMsg='" + message + '\'' +
                '}';
    }
}
