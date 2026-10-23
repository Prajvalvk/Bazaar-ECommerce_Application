package com.ecommerce.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Cart {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartId;
    @OneToOne(optional = false) @JoinColumn(name = "user_id", unique = true)
    private User user;
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();
}
