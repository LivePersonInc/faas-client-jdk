package com.liveperson.faas.response.lambda;

import java.util.Objects;

public class KeyValuePairResponseObject {

	private String key;
	private String value;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		KeyValuePairResponseObject that = (KeyValuePairResponseObject) o;
		return Objects.equals(key, that.key) &&
				Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(key, value);
	}

	@Override
	public String toString() {
		return "KeyValuePairResponseObject{" +
				"key='" + key + '\'' +
				", value='" + value + '\'' +
				'}';
	}
}
