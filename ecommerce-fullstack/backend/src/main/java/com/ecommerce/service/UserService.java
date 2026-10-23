package com.ecommerce.service;

import com.ecommerce.config.PasswordHasher;
import com.ecommerce.entity.*;
import com.ecommerce.exception.ApiException;
import com.ecommerce.repository.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class UserService {
    private final UserRepository repo;
    private final PasswordHasher encoder;

    @Transactional
    public User create(User u) {
        if (repo.existsByEmail(u.getEmail())) throw ApiException.conflict("Email already registered: " + u.getEmail());
        u.setUserId(null);
        if (u.getPassword() != null && !u.getPassword().isBlank()) u.setPassword(encoder.encode(u.getPassword()));
        return repo.save(u);
    }
    public List<User> all() { return repo.findAll(); }
    public User get(Long id) { return repo.findById(id).orElseThrow(() -> ApiException.notFound("User not found: " + id)); }

    @Transactional
    public User update(Long id, User in) {
        User u = get(id);
        repo.findByEmail(in.getEmail()).filter(x -> !x.getUserId().equals(id))
            .ifPresent(x -> { throw ApiException.conflict("Email already in use: " + in.getEmail()); });
        u.setName(in.getName());
        u.setEmail(in.getEmail());
        if (in.getPassword() != null && !in.getPassword().isBlank()) u.setPassword(encoder.encode(in.getPassword()));
        return repo.save(u);
    }
    public void delete(Long id) { repo.delete(get(id)); }

    public User resolve(User ref) {
        if (ref == null || ref.getUserId() == null) throw ApiException.badRequest("user.userId is required");
        return get(ref.getUserId());
    }
    public User login(String email, String password) {
        User u = repo.findByEmail(email).orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));
        if (u.getPassword() == null || !encoder.matches(password, u.getPassword()))
            throw ApiException.unauthorized("Invalid email or password");
        return u;
    }
}
