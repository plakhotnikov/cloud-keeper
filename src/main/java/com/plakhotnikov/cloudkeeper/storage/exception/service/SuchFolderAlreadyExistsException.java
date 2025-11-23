package com.plakhotnikov.cloudkeeper.storage.exception.service;

public class SuchFolderAlreadyExistsException extends MinioServiceException{
    public SuchFolderAlreadyExistsException(String message) {
        super(message);
    }

}
