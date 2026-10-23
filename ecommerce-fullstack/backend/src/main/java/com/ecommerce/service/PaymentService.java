package com.ecommerce.service;

import com.ecommerce.entity.*;
import com.ecommerce.exception.ApiException;
import com.ecommerce.repository.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository repo;
    private final UserService users;

    @Transactional
    public Payment create(Payment p) {
        p.setPaymentId(null);
        p.setUser(users.resolve(p.getUser()));
        return repo.save(p);
    }
    public List<Payment> all() { return repo.findAll(); }
    public Payment get(Long id) { return repo.findById(id).orElseThrow(() -> ApiException.notFound("Payment not found: " + id)); }

    @Transactional
    public Payment update(Long id, Payment in) {
        Payment p = get(id);
        p.setMethod(in.getMethod()); p.setAmount(in.getAmount());
        if (in.getUser() != null) p.setUser(users.resolve(in.getUser()));
        return repo.save(p);
    }
    public void delete(Long id) { repo.delete(get(id)); }
}
