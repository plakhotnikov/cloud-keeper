package com.plakhotnikov.cloudkeeper.storage.exception.service;

public class MinioServiceException extends RuntimeException{
    public MinioServiceException(String message) {
        super(message);
    }

    public MinioServiceException(Throwable cause) {
        super(cause);
    }
}
