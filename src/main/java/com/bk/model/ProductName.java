package com.bk.model;

public enum ProductName {
    BREAD("Bread", "/images/bread.png"),
    CHAPATI("Chapati", "/images/chapati.jpg"),
    ANDAZI("Andazi", "/images/andazi.jpg");

    private final String displayName;
    private final String imagePath;

    ProductName(String displayName, String imagePath) {
        this.displayName = displayName;
        this.imagePath = imagePath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getImagePath() {
        return imagePath;
    }
}
