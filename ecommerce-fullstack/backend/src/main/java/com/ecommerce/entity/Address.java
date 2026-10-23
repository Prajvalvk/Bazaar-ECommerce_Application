package com.ecommerce.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Address {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;
    @NotBlank(message = "city is required") private String city;
    @NotBlank(message = "state is required") private String state;
    @NotBlank(message = "country is required") private String country;
    @ManyToOne(optional = false) @JoinColumn(name = "user_id")
    private User user;
}
