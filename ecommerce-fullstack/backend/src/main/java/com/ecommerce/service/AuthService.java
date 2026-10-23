package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.*;
import com.ecommerce.exception.ApiException;
import com.ecommerce.repository.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class AuthService {
    private final UserService users;
    private final com.ecommerce.config.TokenService tokens;
    @org.springframework.beans.factory.annotation.Value("${ADMIN_SIGNUP_CODE:}")
    private String adminCode;

    public Map<String, Object> register(RegisterRequest r) {
        return session(create(r.name(), r.email(), r.password(), Role.USER));
    }

    public Map<String, Object> registerAdmin(AdminRegisterRequest r) {
        if (adminCode == null || adminCode.isBlank()) throw ApiException.forbidden("Admin sign up is disabled on this server");
        if (!adminCode.equals(r.adminCode())) throw ApiException.forbidden("Invalid admin access code");
        return session(create(r.name(), r.email(), r.password(), Role.ADMIN));
    }

    public Map<String, Object> login(LoginRequest r, Role expected) {
        User u = users.login(r.email(), r.password());
        if (u.getRole() != expected) {
            throw ApiException.forbidden(expected == Role.ADMIN
                    ? "This account is not an admin. Use the customer sign in."
                    : "This is an admin account. Use the admin sign in.");
        }
        return session(u);
    }

    private User create(String name, String email, String password, Role role) {
        User u = new User();
        u.setName(name); u.setEmail(email); u.setPassword(password); u.setRole(role);
        return users.create(u);
    }

    private Map<String, Object> session(User u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("token", tokens.issue(u.getUserId()));
        m.put("user", u);
        return m;
    }
}
