package com.suraj.scheduler.exception;

public class DependencyCycleException {
		private String message;

	public DependencyCycleException(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
