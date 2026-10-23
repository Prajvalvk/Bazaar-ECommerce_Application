package com.ecommerce.dto;

import java.util.List;

public record OrderRequest(Long addressId, String paymentMethod, List<OrderItemRequest> items) {}
