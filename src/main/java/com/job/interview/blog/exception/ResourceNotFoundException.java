package com.job.interview.blog.exception;

public class ResourceNotFoundException extends CustomException{
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}