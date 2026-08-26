package com.interviewlab.exception;

public class NoRollbackException extends RuntimeException {

    public NoRollbackException(String message) {
        super(message);
    }
}