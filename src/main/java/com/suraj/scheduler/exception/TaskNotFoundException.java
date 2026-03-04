package com.suraj.scheduler.exception;

public class TaskNotFoundException {
	private String message;

	public TaskNotFoundException(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
