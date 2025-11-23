package com.plakhotnikov.cloudkeeper.storage.exception.repository;

public class UploadException extends MinioRepositoryException{
    public UploadException(String message) {
        super(message);
    }
}
