package com.ecommerce.controller;

import com.ecommerce.entity.*;
import com.ecommerce.service.*;
import jakarta.validation.Valid;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService service;

    @PostMapping
    public ResponseEntity<Payment> create(@Valid @RequestBody Payment body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(body));
    }
    @GetMapping
    public ResponseEntity<List<Payment>> getAll() { return ResponseEntity.ok(service.all()); }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getById(@PathVariable Long id) { return ResponseEntity.ok(service.get(id)); }

    @PutMapping("/{id}")
    public ResponseEntity<Payment> update(@PathVariable Long id, @Valid @RequestBody Payment body) {
        return ResponseEntity.ok(service.update(id, body));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Payment " + id + " deleted successfully"));
    }
}
