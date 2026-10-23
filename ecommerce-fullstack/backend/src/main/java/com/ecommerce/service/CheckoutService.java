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
public class CheckoutService {
    private final CartService carts;
    private final CartRepository cartRepo;
    private final OrderService orders;

    @Transactional
    public Order checkout(User me, CheckoutRequest r) {
        Cart cart = carts.mine(me.getUserId());
        if (cart.getItems().isEmpty()) throw ApiException.badRequest("Your cart is empty");
        List<OrderItemRequest> items = cart.getItems().stream()
                .map(i -> new OrderItemRequest(i.getProduct().getProductId(), i.getQuantity()))
                .collect(Collectors.toList());
        Order o = orders.place(me, r.addressId(), r.paymentMethod(), items);
        cart.getItems().clear();
        cartRepo.save(cart);
        return o;
    }
}
