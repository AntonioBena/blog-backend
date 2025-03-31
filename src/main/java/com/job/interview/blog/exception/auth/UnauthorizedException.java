package com.job.interview.blog.exception.auth;

import com.job.interview.blog.exception.CustomException;

public class UnauthorizedException extends CustomException {
    public UnauthorizedException(String message) {
        super(message);
    }
}