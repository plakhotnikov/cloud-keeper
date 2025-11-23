package com.plakhotnikov.cloudkeeper.storage.exception.repository;

public class ObjectDoesNotExistException extends MinioRepositoryException{
    public ObjectDoesNotExistException(String message) {
        super(message);
    }

}
