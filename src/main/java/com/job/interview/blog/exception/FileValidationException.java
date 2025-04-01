package com.job.interview.blog.exception;

public class FileValidationException extends CustomException{
    public FileValidationException(String message) {
        super(message);
    }

    public FileValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}