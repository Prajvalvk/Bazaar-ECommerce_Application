package com.ecommerce.controller;

import com.ecommerce.entity.*;
import com.ecommerce.service.*;
import jakarta.validation.Valid;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService service;

    @PostMapping
    public ResponseEntity<ProductCategory> create(@Valid @RequestBody ProductCategory body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(body));
    }
    @GetMapping
    public ResponseEntity<List<ProductCategory>> getAll() { return ResponseEntity.ok(service.all()); }

    @GetMapping("/{id}")
    public ResponseEntity<ProductCategory> getById(@PathVariable Long id) { return ResponseEntity.ok(service.get(id)); }

    @PutMapping("/{id}")
    public ResponseEntity<ProductCategory> update(@PathVariable Long id, @Valid @RequestBody ProductCategory body) {
        return ResponseEntity.ok(service.update(id, body));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "ProductCategory " + id + " deleted successfully"));
    }
}
