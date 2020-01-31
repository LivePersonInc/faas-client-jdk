package com.liveperson.faas.response.lambda;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LambdaResponse {

	private String name;
	private String uuid;
	private String version;
	private String description;
	private String eventId;
	private String state;
	private RuntimeResponseObject runtime;
	private String createdAt;
	private String createdBy;
	private String updatedAt;
	private String updatedBy;
	private LastDeploymentResponseObject lastDeployment;
	private ImplementationResponseObject implementation;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public RuntimeResponseObject getRuntime() {
		return runtime;
	}

	public void setRuntime(RuntimeResponseObject runtime) {
		this.runtime = runtime;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public LastDeploymentResponseObject getLastDeployment() {
		return lastDeployment;
	}

	public void setLastDeployment(LastDeploymentResponseObject lastDeployment) {
		this.lastDeployment = lastDeployment;
	}

	public ImplementationResponseObject getImplementation() {
		return implementation;
	}

	public void setImplementation(ImplementationResponseObject implementation) {
		this.implementation = implementation;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LambdaResponse that = (LambdaResponse) o;
		return Objects.equals(name, that.name) &&
				Objects.equals(uuid, that.uuid) &&
				Objects.equals(version, that.version) &&
				Objects.equals(description, that.description) &&
				Objects.equals(eventId, that.eventId) &&
				Objects.equals(state, that.state) &&
				Objects.equals(runtime, that.runtime) &&
				Objects.equals(createdAt, that.createdAt) &&
				Objects.equals(createdBy, that.createdBy) &&
				Objects.equals(updatedAt, that.updatedAt) &&
				Objects.equals(updatedBy, that.updatedBy) &&
				Objects.equals(lastDeployment, that.lastDeployment) &&
				Objects.equals(implementation, that.implementation);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, uuid, version, description, eventId, state, runtime, createdAt, createdBy, updatedAt, updatedBy, lastDeployment, implementation);
	}

	@Override
	public String toString() {
		return "LambdaResponse{" +
				"name='" + name + '\'' +
				", uuid='" + uuid + '\'' +
				", version='" + version + '\'' +
				", description='" + description + '\'' +
				", eventId='" + eventId + '\'' +
				", state='" + state + '\'' +
				", runtime=" + runtime +
				", createdAt='" + createdAt + '\'' +
				", createdBy='" + createdBy + '\'' +
				", updatedAt='" + updatedAt + '\'' +
				", updatedBy='" + updatedBy + '\'' +
				", lastDeployment=" + lastDeployment +
				", implementation=" + implementation +
				'}';
	}
}
