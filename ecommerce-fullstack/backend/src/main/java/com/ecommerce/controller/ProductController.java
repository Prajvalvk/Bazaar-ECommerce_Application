package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.entity.*;
import com.ecommerce.exception.ApiException;
import com.ecommerce.service.*;
import jakarta.validation.Valid;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService service;

    /** Admin only. */
    @PostMapping
    public ResponseEntity<Product> create(@Valid @RequestBody Product body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(body));
    }
    /** Admin sees every product; customers see ACTIVE and OUT_OF_STOCK only. */
    @GetMapping
    public ResponseEntity<List<Product>> list(@RequestAttribute("authUser") User me) {
        return ResponseEntity.ok(service.listFor(me));
    }
    @GetMapping("/{id}")
    public ResponseEntity<Product> get(@RequestAttribute("authUser") User me, @PathVariable Long id) {
        return ResponseEntity.ok(service.getFor(me, id));
    }
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Product>> byCategory(@RequestAttribute("authUser") User me, @PathVariable Long categoryId) {
        return ResponseEntity.ok(service.byCategoryFor(me, categoryId));
    }
    /** Admin only. */
    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id, @Valid @RequestBody Product body) {
        return ResponseEntity.ok(service.update(id, body));
    }
    /** Admin only. Body: {"status":"ACTIVE" | "OUT_OF_STOCK" | "INACTIVE"} */
    @PutMapping("/{id}/status")
    public ResponseEntity<Product> setStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String raw = body == null ? null : body.get("status");
        ProductStatus status;
        try {
            status = ProductStatus.valueOf(raw.trim().toUpperCase());
        } catch (Exception e) {
            throw ApiException.badRequest("status must be ACTIVE, OUT_OF_STOCK or INACTIVE");
        }
        return ResponseEntity.ok(service.setStatus(id, status));
    }
    /** Admin only. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Product " + id + " deleted successfully"));
    }
}
