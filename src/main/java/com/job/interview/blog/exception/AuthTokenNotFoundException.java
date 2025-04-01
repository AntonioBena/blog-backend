package com.job.interview.blog.exception;

public class AuthTokenNotFoundException extends CustomException{
    public AuthTokenNotFoundException(String message) {
        super(message);
    }
}