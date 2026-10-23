package com.ecommerce.config;

import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ApiException;
import com.ecommerce.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Every request needs a valid "Authorization: Bearer <token>" header, except health and sign in/up.
 * Admin-only areas are enforced here. Ownership of orders, addresses and carts is enforced in the services.
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    private final TokenService tokens;
    private final UserRepository users;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        String method = req.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) return true;
        String path = req.getRequestURI();

        User user = null;
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            Long id = tokens.verify(header.substring(7).trim());
            if (id != null) user = users.findById(id).orElse(null);
        }
        if (user != null) req.setAttribute("authUser", user);

        boolean open = path.equals("/") || path.equals("/health") || path.equals("/error")
                || (path.startsWith("/auth/") && !path.equals("/auth/me"));
        if (open) return true;

        if (user == null) throw ApiException.unauthorized("Please sign in to continue");
        if (adminOnly(method, path) && user.getRole() != Role.ADMIN) {
            throw ApiException.forbidden("Admin access required");
        }
        return true;
    }

    private boolean adminOnly(String method, String path) {
        if (path.startsWith("/admin") || path.startsWith("/users") || path.startsWith("/payments")) return true;
        if (path.equals("/tracking")) return true;
        boolean write = !"GET".equalsIgnoreCase(method);
        return write && (path.startsWith("/products") || path.startsWith("/categories") || path.startsWith("/tracking"));
    }
}
