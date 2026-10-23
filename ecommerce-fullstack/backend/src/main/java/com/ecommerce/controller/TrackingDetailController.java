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
@RequestMapping("/tracking")
@RequiredArgsConstructor
public class TrackingDetailController {
    private final TrackingDetailService service;

    /** Admin only. Also updates the order status. */
    @PostMapping
    public ResponseEntity<TrackingDetail> create(@Valid @RequestBody TrackingDetail body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(body));
    }
    /** Admin only. */
    @GetMapping
    public ResponseEntity<List<TrackingDetail>> getAll() { return ResponseEntity.ok(service.all()); }

    /** Tracking history of one order (owner or admin). */
    @GetMapping("/{orderId}")
    public ResponseEntity<List<TrackingDetail>> byOrder(@RequestAttribute("authUser") User me, @PathVariable Long orderId) {
        return ResponseEntity.ok(service.byOrder(me, orderId));
    }
    @PutMapping("/{id}")
    public ResponseEntity<TrackingDetail> update(@PathVariable Long id, @Valid @RequestBody TrackingDetail body) {
        return ResponseEntity.ok(service.update(id, body));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Tracking detail " + id + " deleted successfully"));
    }
}
