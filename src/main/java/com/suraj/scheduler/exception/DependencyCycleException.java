package com.suraj.scheduler.exception;


public class DependencyCycleException extends RuntimeException {

    public DependencyCycleException(String message) {
        super(message);
    }
}
