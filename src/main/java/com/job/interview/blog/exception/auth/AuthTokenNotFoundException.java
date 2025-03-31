package com.job.interview.blog.exception.auth;

import com.job.interview.blog.exception.CustomException;

public class AuthTokenNotFoundException extends CustomException {
    public AuthTokenNotFoundException(String message) {
        super(message);
    }

    public AuthTokenNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}