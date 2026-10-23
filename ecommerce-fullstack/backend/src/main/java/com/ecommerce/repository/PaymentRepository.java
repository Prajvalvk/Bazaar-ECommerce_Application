package com.ecommerce.repository;

import com.ecommerce.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrderOrderNo(Long orderNo);
    void deleteByOrderOrderNo(Long orderNo);
}
