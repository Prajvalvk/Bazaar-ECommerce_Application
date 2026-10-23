package com.ecommerce.controller;

import com.ecommerce.entity.*;
import com.ecommerce.service.*;
import jakarta.validation.Valid;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(body));
    }
    @GetMapping
    public ResponseEntity<List<User>> getAll() { return ResponseEntity.ok(service.all()); }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) { return ResponseEntity.ok(service.get(id)); }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @Valid @RequestBody User body) {
        return ResponseEntity.ok(service.update(id, body));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "User " + id + " deleted successfully"));
    }
}
