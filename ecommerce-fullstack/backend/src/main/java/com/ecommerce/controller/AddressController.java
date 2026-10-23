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
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService service;

    @PostMapping
    public ResponseEntity<Address> create(@RequestAttribute("authUser") User me, @Valid @RequestBody Address body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(me, body));
    }
    @GetMapping
    public ResponseEntity<List<Address>> mine(@RequestAttribute("authUser") User me) { return ResponseEntity.ok(service.mine(me)); }

    @GetMapping("/{id}")
    public ResponseEntity<Address> get(@RequestAttribute("authUser") User me, @PathVariable Long id) {
        return ResponseEntity.ok(service.getOwned(me, id));
    }
    @PutMapping("/{id}")
    public ResponseEntity<Address> update(@RequestAttribute("authUser") User me, @PathVariable Long id, @Valid @RequestBody Address body) {
        return ResponseEntity.ok(service.update(me, id, body));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@RequestAttribute("authUser") User me, @PathVariable Long id) {
        service.delete(me, id);
        return ResponseEntity.ok(Map.of("message", "Address " + id + " deleted successfully"));
    }
}
