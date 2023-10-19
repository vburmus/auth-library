package com.vburmus.auth.utils.exceptionhandler.exceptions;

public class RestApiServerException extends RuntimeException {
    public RestApiServerException(String message) {
        super(message);
    }
}