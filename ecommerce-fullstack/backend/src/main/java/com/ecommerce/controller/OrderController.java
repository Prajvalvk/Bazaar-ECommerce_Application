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
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService service;

    /** Make an order by selecting products: {addressId, paymentMethod, items:[{productId, quantity}]} */
    @PostMapping
    public ResponseEntity<Order> place(@RequestAttribute("authUser") User me, @RequestBody OrderRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.place(me, r.addressId(), r.paymentMethod(), r.items()));
    }
    /** Customers get their own orders; admins get all orders. */
    @GetMapping
    public ResponseEntity<List<Order>> list(@RequestAttribute("authUser") User me) { return ResponseEntity.ok(service.listFor(me)); }

    @GetMapping("/{id}")
    public ResponseEntity<Order> get(@RequestAttribute("authUser") User me, @PathVariable Long id) {
        return ResponseEntity.ok(service.getFor(me, id));
    }
    @PutMapping("/{id}")
    public ResponseEntity<Order> update(@RequestAttribute("authUser") User me, @PathVariable Long id, @RequestBody OrderRequest r) {
        return ResponseEntity.ok(service.update(me, id, r));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@RequestAttribute("authUser") User me, @PathVariable Long id) {
        service.delete(me, id);
        return ResponseEntity.ok(Map.of("message", "Order " + id + " cancelled successfully"));
    }
}
