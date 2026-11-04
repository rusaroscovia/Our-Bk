package com.bk.repository;

import com.bk.model.Product;
import com.bk.model.ProductName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByName(ProductName name);
}
