package com.liveperson.faas.response.lambda;

import java.util.Objects;

public class LastDeploymentResponseObject {

	private String uuid;
	private String name;
	private String lambdaUUID;
	private String lambdaVersion;
	private String imageName;
	private String deploymentState;
	private DeploymentErrorResponseObject deploymentError;
	private String createdAt;
	private String updatedAt;
	private String deployedAt;
	private String createdBy;
	private String updatedBy;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLambdaUUID() {
		return lambdaUUID;
	}

	public void setLambdaUUID(String lambdaUUID) {
		this.lambdaUUID = lambdaUUID;
	}

	public String getLambdaVersion() {
		return lambdaVersion;
	}

	public void setLambdaVersion(String lambdaVersion) {
		this.lambdaVersion = lambdaVersion;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public String getDeploymentState() {
		return deploymentState;
	}

	public void setDeploymentState(String deploymentState) {
		this.deploymentState = deploymentState;
	}

	public DeploymentErrorResponseObject getDeploymentError() {
		return deploymentError;
	}

	public void setDeploymentError(DeploymentErrorResponseObject deploymentError) {
		this.deploymentError = deploymentError;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getDeployedAt() {
		return deployedAt;
	}

	public void setDeployedAt(String deployedAt) {
		this.deployedAt = deployedAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LastDeploymentResponseObject that = (LastDeploymentResponseObject) o;
		return Objects.equals(uuid, that.uuid) &&
				Objects.equals(name, that.name) &&
				Objects.equals(lambdaUUID, that.lambdaUUID) &&
				Objects.equals(lambdaVersion, that.lambdaVersion) &&
				Objects.equals(imageName, that.imageName) &&
				Objects.equals(deploymentState, that.deploymentState) &&
				Objects.equals(deploymentError, that.deploymentError) &&
				Objects.equals(createdAt, that.createdAt) &&
				Objects.equals(updatedAt, that.updatedAt) &&
				Objects.equals(deployedAt, that.deployedAt) &&
				Objects.equals(createdBy, that.createdBy) &&
				Objects.equals(updatedBy, that.updatedBy);
	}

	@Override
	public int hashCode() {
		return Objects.hash(uuid, name, lambdaUUID, lambdaVersion, imageName, deploymentState, deploymentError, createdAt, updatedAt, deployedAt, createdBy, updatedBy);
	}

	@Override
	public String toString() {
		return "LastDeploymentResponseObject{" +
				"uuid='" + uuid + '\'' +
				", name='" + name + '\'' +
				", lambdaUUID='" + lambdaUUID + '\'' +
				", lambdaVersion='" + lambdaVersion + '\'' +
				", imageName='" + imageName + '\'' +
				", deploymentState='" + deploymentState + '\'' +
				", deploymentError=" + deploymentError +
				", createdAt='" + createdAt + '\'' +
				", updatedAt='" + updatedAt + '\'' +
				", deployedAt='" + deployedAt + '\'' +
				", createdBy='" + createdBy + '\'' +
				", updatedBy='" + updatedBy + '\'' +
				'}';
	}
}
