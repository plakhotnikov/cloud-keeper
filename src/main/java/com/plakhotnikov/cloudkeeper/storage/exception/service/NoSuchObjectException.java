package com.plakhotnikov.cloudkeeper.storage.exception.service;

import com.plakhotnikov.cloudkeeper.storage.util.PathUtils;

public class NoSuchObjectException extends MinioServiceException{
    public NoSuchObjectException(String objPath) {
        super("File or folder '%s' does not exist".formatted(PathUtils.removeUserPrefix(objPath)));
    }
}
