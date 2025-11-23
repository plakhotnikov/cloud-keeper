package com.plakhotnikov.cloudkeeper.storage.exception.repository;

public class DeleteException extends MinioRepositoryException {
    public DeleteException(String message) {
        super(message);
    }

}
