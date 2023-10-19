package com.vburmus.auth.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AuthConstants {
    public static final String AUTHENTICATION_BEARER_TOKEN = "Bearer ";
    public static final String API_CALL_ERROR = "Error while making API call to: ";
    public static final String INTERNAL_SERVER_ERROR = "An internal server " +
            "error occurred while processing the request.";
    public static final String GENERIC_EXCEPTION = "Generic exception";
    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";
}
