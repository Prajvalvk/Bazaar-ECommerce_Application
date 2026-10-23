package com.ecommerce.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TrackingDetail {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trackingId;
    @NotBlank(message = "status is required") private String status;
    @ManyToOne(optional = false) @JoinColumn(name = "order_no")
    private Order order;
}
