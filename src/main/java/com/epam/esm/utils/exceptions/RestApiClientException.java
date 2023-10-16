package com.epam.esm.utils.exceptions;

public class RestApiClientException extends RuntimeException {
    public RestApiClientException(String message) {
        super(message);
    }
}