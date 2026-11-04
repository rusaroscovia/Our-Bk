package com.bk.model;

public enum ProductName {
    BREAD("Bread"),
    CHAPATI("Chapati"),
    ANDAZI("Andazi");

    private final String displayName;

    ProductName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
