package com.ecommerce.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;
    @NotBlank(message = "method is required") private String method;
    @NotNull(message = "amount is required") @Positive(message = "amount must be positive")
    private Double amount;
    @ManyToOne(optional = false) @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne @JoinColumn(name = "order_no")
    @JsonIgnoreProperties({"items", "user", "shippingAddress"})
    private Order order;
}
