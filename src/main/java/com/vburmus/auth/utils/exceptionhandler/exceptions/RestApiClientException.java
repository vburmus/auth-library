package com.vburmus.auth.utils.exceptionhandler.exceptions;

public class RestApiClientException extends RuntimeException {
    public RestApiClientException(String message) {
        super(message);
    }
}