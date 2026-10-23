package com.ecommerce.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

@Entity @Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    @NotBlank(message = "name is required")
    private String name;
    @NotBlank(message = "email is required") @Email(message = "invalid email")
    @Column(unique = true, nullable = false)
    private String email;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    public Role getRole() { return role == null ? Role.USER : role; }
}
