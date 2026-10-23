package com.ecommerce.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

@Entity @Table(name = "order_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "order_no") @JsonIgnore
    private Order order;
    @ManyToOne(optional = false) @JoinColumn(name = "product_id")
    private Product product;
    private int quantity;
    private Double unitPrice;
}
