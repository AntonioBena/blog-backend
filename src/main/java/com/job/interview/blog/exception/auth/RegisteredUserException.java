package com.job.interview.blog.exception.auth;

import com.job.interview.blog.exception.CustomException;

public class RegisteredUserException extends CustomException {
    public RegisteredUserException(String message) {
        super(message);
    }
}