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
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService service;

    @GetMapping
    public ResponseEntity<Cart> mine(@RequestAttribute("authUser") User me) { return ResponseEntity.ok(service.mine(me.getUserId())); }

    @PostMapping("/add")
    public ResponseEntity<Cart> add(@RequestAttribute("authUser") User me, @Valid @RequestBody CartRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.add(me.getUserId(), r.productId(), r.quantity()));
    }
    @PutMapping("/update")
    public ResponseEntity<Cart> update(@RequestAttribute("authUser") User me, @Valid @RequestBody CartRequest r) {
        return ResponseEntity.ok(service.update(me.getUserId(), r.productId(), r.quantity()));
    }
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Cart> remove(@RequestAttribute("authUser") User me, @PathVariable Long productId) {
        return ResponseEntity.ok(service.remove(me.getUserId(), productId));
    }
}
