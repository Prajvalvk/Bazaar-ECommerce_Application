package com.ecommerce.controller;

import com.ecommerce.entity.*;
import com.ecommerce.service.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService users;
    private final OrderService orders;
    private final PaymentService payments;
    private final ProductService products;
    private final CategoryService categories;

    @GetMapping("/users") public ResponseEntity<List<User>> users() { return ResponseEntity.ok(users.all()); }
    @GetMapping("/orders") public ResponseEntity<List<Order>> orders() { return ResponseEntity.ok(orders.all()); }
    @GetMapping("/payments") public ResponseEntity<List<Payment>> payments() { return ResponseEntity.ok(payments.all()); }
    @GetMapping("/products") public ResponseEntity<List<Product>> products() { return ResponseEntity.ok(products.all()); }
    @GetMapping("/categories") public ResponseEntity<List<ProductCategory>> categories() { return ResponseEntity.ok(categories.all()); }
}
