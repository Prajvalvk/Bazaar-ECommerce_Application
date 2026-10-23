package com.ecommerce.repository;

import com.ecommerce.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryCategoryId(Long categoryId);
}
