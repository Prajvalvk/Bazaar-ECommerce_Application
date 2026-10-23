package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.*;
import com.ecommerce.exception.ApiException;
import com.ecommerce.repository.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class TrackingDetailService {
    private final TrackingDetailRepository repo;
    private final OrderService orders;

    private static String norm(String s) { return s.trim().toUpperCase().replaceAll("\\s+", "_"); }

    @Transactional
    public TrackingDetail create(TrackingDetail t) {
        if (t.getOrder() == null || t.getOrder().getOrderNo() == null) throw ApiException.badRequest("order.orderNo is required");
        Order o = orders.get(t.getOrder().getOrderNo());
        t.setTrackingId(null);
        t.setOrder(o);
        t.setStatus(norm(t.getStatus()));
        TrackingDetail saved = repo.save(t);
        orders.setStatus(o.getOrderNo(), saved.getStatus());
        return saved;
    }
    public List<TrackingDetail> all() { return repo.findAll(); }
    public List<TrackingDetail> byOrder(User me, Long orderId) {
        orders.getFor(me, orderId);
        return repo.findByOrderOrderNo(orderId);
    }
    public TrackingDetail get(Long id) { return repo.findById(id).orElseThrow(() -> ApiException.notFound("Tracking detail not found: " + id)); }

    @Transactional
    public TrackingDetail update(Long id, TrackingDetail in) {
        TrackingDetail t = get(id);
        t.setStatus(norm(in.getStatus()));
        TrackingDetail saved = repo.save(t);
        orders.setStatus(saved.getOrder().getOrderNo(), saved.getStatus());
        return saved;
    }
    @Transactional
    public void delete(Long id) { repo.delete(get(id)); }
}
