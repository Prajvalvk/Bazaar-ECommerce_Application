package com.ecommerce.repository;

import com.ecommerce.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    void deleteByProductProductId(Long productId);
}
