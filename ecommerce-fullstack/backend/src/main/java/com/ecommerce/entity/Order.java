package com.ecommerce.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

@Entity @Table(name = "orders")
@Getter @Setter @NoArgsConstructor
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderNo;
    private Double orderAmount;
    private LocalDate orderDate;
    private String status;
    @ManyToOne(optional = false) @JoinColumn(name = "user_id")
    private User user;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    @ManyToOne @JoinColumn(name = "shipping_address_id")
    private Address shippingAddress;
}
