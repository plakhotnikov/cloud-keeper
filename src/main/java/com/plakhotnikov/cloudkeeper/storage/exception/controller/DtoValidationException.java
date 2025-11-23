package com.plakhotnikov.cloudkeeper.storage.exception.controller;

public class DtoValidationException extends RuntimeException{
    public DtoValidationException(String message) {
        super(message);
    }
}
