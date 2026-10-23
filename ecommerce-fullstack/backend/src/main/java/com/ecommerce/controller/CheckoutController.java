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
public class CheckoutController {
    private final CheckoutService service;

    /** Turns the current cart into an order. */
    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(@RequestAttribute("authUser") User me, @Valid @RequestBody CheckoutRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.checkout(me, r));
    }
}
