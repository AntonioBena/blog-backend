package com.job.interview.blog.exception.auth;

import com.job.interview.blog.exception.CustomException;

public class UserNotFoundException extends CustomException {
    public UserNotFoundException(String message) {
        super(message);
    }
}