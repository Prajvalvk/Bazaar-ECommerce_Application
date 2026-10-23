package com.ecommerce.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;
    @NotBlank(message = "name is required") private String name;
    @NotNull(message = "price is required") @PositiveOrZero(message = "price cannot be negative")
    private Double price;
    @Column(length = 1000) private String description;
    @Enumerated(EnumType.STRING)
    private ProductStatus status = ProductStatus.ACTIVE;
    @ManyToOne @JoinColumn(name = "category_id")
    private ProductCategory category;

    public ProductStatus getStatus() { return status == null ? ProductStatus.ACTIVE : status; }
}
