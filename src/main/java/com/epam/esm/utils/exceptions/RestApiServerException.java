package com.epam.esm.utils.exceptions;

public class RestApiServerException extends RuntimeException {
    public RestApiServerException(String message) {
        super(message);
    }
}