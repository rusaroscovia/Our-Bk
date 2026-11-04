package com.bk.service;

import com.bk.model.Product;
import com.bk.model.ProductName;
import com.bk.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public void createDefaultProducts() {
        for (ProductName name : ProductName.values()) {
            if (productRepository.findByName(name).isEmpty()) {
                productRepository.save(new Product(name, defaultPrice(name)));
            }
        }
    }

    @Transactional(readOnly = true)
    public List<Product> getProducts() {
        return Arrays.stream(ProductName.values())
                .map(name -> productRepository.findByName(name)
                        .orElseThrow(() -> new ResourceNotFoundException("Product", name)))
                .toList();
    }

    @Transactional
    public Product updatePrice(ProductName name, BigDecimal priceRwf) {
        validateAmount(priceRwf, "Product price");
        Product product = productRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Product", name));
        product.setPriceRwf(priceRwf);
        return productRepository.save(product);
    }

    private BigDecimal defaultPrice(ProductName name) {
        return switch (name) {
            case BREAD -> BigDecimal.valueOf(1700);
            case CHAPATI, ANDAZI -> BigDecimal.valueOf(200);
        };
    }

    static void validateAmount(BigDecimal amountRwf, String label) {
        if (amountRwf == null || amountRwf.signum() <= 0 || amountRwf.scale() > 0) {
            throw new IllegalArgumentException(label + " must be a positive whole-number RWF amount.");
        }
    }
}
