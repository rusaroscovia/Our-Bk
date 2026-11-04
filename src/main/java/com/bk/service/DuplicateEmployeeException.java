package com.bk.service;

public class DuplicateEmployeeException extends ServiceException {

    public DuplicateEmployeeException(String name) {
        super("An employee named '" + name.trim() + "' is already registered.");
    }
}
