package com.liveperson.faas.response.lambda;

import java.util.Objects;

public class RuntimeResponseObject {

	private String uuid;
	private String name;
	private String baseImageName;

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

	public String getBaseImageName() {
		return baseImageName;
	}

	public void setBaseImageName(String baseImageName) {
		this.baseImageName = baseImageName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RuntimeResponseObject that = (RuntimeResponseObject) o;
		return Objects.equals(uuid, that.uuid) &&
				Objects.equals(name, that.name) &&
				Objects.equals(baseImageName, that.baseImageName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(uuid, name, baseImageName);
	}

	@Override
	public String toString() {
		return "RuntimeResponseObject{" +
				"uuid='" + uuid + '\'' +
				", name='" + name + '\'' +
				", baseImageName='" + baseImageName + '\'' +
				'}';
	}
}
