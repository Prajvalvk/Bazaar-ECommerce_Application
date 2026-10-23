package com.ecommerce.repository;

import com.ecommerce.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserUserId(Long userId);
}
