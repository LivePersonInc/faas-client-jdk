package com.liveperson.faas.response.lambda;

import java.util.List;
import java.util.Objects;

public class ImplementationResponseObject {

	private String code;
	private List<KeyValuePairResponseObject> dependencies;
	private List<KeyValuePairResponseObject> environmentVariables;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public List<KeyValuePairResponseObject> getDependencies() {
		return dependencies;
	}

	public void setDependencies(List<KeyValuePairResponseObject> dependencies) {
		this.dependencies = dependencies;
	}

	public List<KeyValuePairResponseObject> getEnvironmentVariables() {
		return environmentVariables;
	}

	public void setEnvironmentVariables(List<KeyValuePairResponseObject> environmentVariables) {
		this.environmentVariables = environmentVariables;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ImplementationResponseObject that = (ImplementationResponseObject) o;
		return Objects.equals(code, that.code) &&
				Objects.equals(dependencies, that.dependencies) &&
				Objects.equals(environmentVariables, that.environmentVariables);
	}

	@Override
	public int hashCode() {
		return Objects.hash(code, dependencies, environmentVariables);
	}

	@Override
	public String toString() {
		return "ImplementationResponseObject{" +
				"code='" + code + '\'' +
				", dependencies=" + dependencies +
				", environmentVariables=" + environmentVariables +
				'}';
	}
}
