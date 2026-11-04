package com.bk.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ProductInitialization implements ApplicationRunner {

    private final ProductService productService;

    public ProductInitialization(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void run(ApplicationArguments args) {
        productService.createDefaultProducts();
    }
}
