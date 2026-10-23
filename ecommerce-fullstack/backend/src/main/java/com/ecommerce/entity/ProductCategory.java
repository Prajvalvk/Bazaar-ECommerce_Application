package com.ecommerce.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProductCategory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;
    @NotBlank(message = "name is required") private String name;
    @OneToMany(mappedBy = "category") @JsonIgnore
    private List<Product> products = new ArrayList<>();
}
