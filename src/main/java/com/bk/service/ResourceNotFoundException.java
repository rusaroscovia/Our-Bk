package com.bk.service;

public class ResourceNotFoundException extends ServiceException {

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " not found: " + id);
    }
}
