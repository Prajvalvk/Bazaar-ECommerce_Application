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
@RequiredArgsConstructor
public class AuthController {
    private final AuthService auth;

    @PostMapping("/auth/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.register(r));
    }
    @PostMapping("/auth/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest r) {
        return ResponseEntity.ok(auth.login(r, Role.USER));
    }
    @PostMapping("/auth/admin/register")
    public ResponseEntity<Map<String, Object>> registerAdmin(@Valid @RequestBody AdminRegisterRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.registerAdmin(r));
    }
    @PostMapping("/auth/admin/login")
    public ResponseEntity<Map<String, Object>> adminLogin(@Valid @RequestBody LoginRequest r) {
        return ResponseEntity.ok(auth.login(r, Role.ADMIN));
    }
    @GetMapping("/auth/me")
    public ResponseEntity<User> me(@RequestAttribute("authUser") User me) { return ResponseEntity.ok(me); }
}
