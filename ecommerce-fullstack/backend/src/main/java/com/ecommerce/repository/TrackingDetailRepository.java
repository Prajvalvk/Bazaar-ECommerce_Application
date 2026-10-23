package com.ecommerce.repository;

import com.ecommerce.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackingDetailRepository extends JpaRepository<TrackingDetail, Long> {
    List<TrackingDetail> findByOrderOrderNo(Long orderNo);
    void deleteByOrderOrderNo(Long orderNo);
}
