package com.liveperson.faas.response.lambda;

import java.util.List;
import java.util.Objects;

public class DeploymentErrorResponseObject {

	private String errorCode;
	private String errorMsg;
	private List<ErrorLogResponseObject> errorLogs;

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

	public List<ErrorLogResponseObject> getErrorLogs() {
		return errorLogs;
	}

	public void setErrorLogs(List<ErrorLogResponseObject> errorLogs) {
		this.errorLogs = errorLogs;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DeploymentErrorResponseObject that = (DeploymentErrorResponseObject) o;
		return Objects.equals(errorCode, that.errorCode) &&
				Objects.equals(errorMsg, that.errorMsg) &&
				Objects.equals(errorLogs, that.errorLogs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(errorCode, errorMsg, errorLogs);
	}

	@Override
	public String toString() {
		return "DeploymentErrorResponseObject{" +
				"errorCode='" + errorCode + '\'' +
				", errorMsg='" + errorMsg + '\'' +
				", errorLogs=" + errorLogs +
				'}';
	}
}
