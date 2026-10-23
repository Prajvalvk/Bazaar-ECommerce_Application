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
public class CartService {
    private final CartRepository repo;
    private final UserService users;
    private final ProductService products;

    private Cart getOrCreate(Long userId) {
        User u = users.get(userId);
        return repo.findByUserUserId(userId).orElseGet(() -> repo.save(new Cart(null, u, new ArrayList<>())));
    }
    private static Optional<CartItem> find(Cart c, Long productId) {
        return c.getItems().stream().filter(i -> i.getProduct().getProductId().equals(productId)).findFirst();
    }

    @Transactional
    public Cart mine(Long userId) { return getOrCreate(userId); }

    @Transactional
    public Cart add(Long userId, Long productId, int qty) {
        if (qty < 1) throw ApiException.badRequest("quantity must be at least 1");
        Cart cart = getOrCreate(userId);
        Product p = products.get(productId);
        if (p.getStatus() != ProductStatus.ACTIVE) throw ApiException.badRequest(p.getName() + " is not available right now");
        Optional<CartItem> existing = find(cart, productId);
        if (existing.isPresent()) existing.get().setQuantity(existing.get().getQuantity() + qty);
        else cart.getItems().add(new CartItem(null, cart, p, qty));
        return repo.save(cart);
    }

    @Transactional
    public Cart update(Long userId, Long productId, int qty) {
        Cart cart = getOrCreate(userId);
        CartItem item = find(cart, productId).orElseThrow(() -> ApiException.notFound("Product " + productId + " is not in the cart"));
        if (qty <= 0) cart.getItems().remove(item); else item.setQuantity(qty);
        return repo.save(cart);
    }

    @Transactional
    public Cart remove(Long userId, Long productId) {
        Cart cart = getOrCreate(userId);
        CartItem item = find(cart, productId).orElseThrow(() -> ApiException.notFound("Product " + productId + " is not in the cart"));
        cart.getItems().remove(item);
        return repo.save(cart);
    }
}
