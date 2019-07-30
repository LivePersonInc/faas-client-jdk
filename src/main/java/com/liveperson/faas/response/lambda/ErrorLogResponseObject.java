package com.liveperson.faas.response.lambda;

import java.util.List;
import java.util.Objects;

public class ErrorLogResponseObject {

	private String level;
	private String message;
	private long timestamp;
	private List<String> extras;

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public List<String> getExtras() {
		return extras;
	}

	public void setExtras(List<String> extras) {
		this.extras = extras;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ErrorLogResponseObject that = (ErrorLogResponseObject) o;
		return timestamp == that.timestamp &&
				Objects.equals(level, that.level) &&
				Objects.equals(message, that.message) &&
				Objects.equals(extras, that.extras);
	}

	@Override
	public int hashCode() {
		return Objects.hash(level, message, timestamp, extras);
	}

	@Override
	public String toString() {
		return "ErrorLogResponseObject{" +
				"level='" + level + '\'' +
				", message='" + message + '\'' +
				", timestamp=" + timestamp +
				", extras=" + extras +
				'}';
	}
}
