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
public class OrderService {
    public static final String PLACED = "ORDER_PLACED";
    private final OrderRepository repo;
    private final TrackingDetailRepository trackingRepo;
    private final PaymentRepository paymentRepo;
    private final ProductService products;
    private final AddressService addresses;

    private static boolean admin(User u) { return u.getRole() == Role.ADMIN; }
    private static String statusOf(Order o) { return o.getStatus() == null ? PLACED : o.getStatus(); }

    /** Create an order from selected products (productId + quantity). */
    @Transactional
    public Order place(User me, Long addressId, String paymentMethod, List<OrderItemRequest> reqs) {
        if (paymentMethod == null || paymentMethod.isBlank()) throw ApiException.badRequest("paymentMethod is required");
        if (addressId == null) throw ApiException.badRequest("addressId is required");
        Address addr = addresses.getOwned(me, addressId);
        Order o = new Order();
        o.setUser(me);
        o.setShippingAddress(addr);
        o.setOrderDate(LocalDate.now());
        o.setStatus(PLACED);
        fillItems(o, reqs);
        o = repo.save(o);
        paymentRepo.save(new Payment(null, paymentMethod.trim(), o.getOrderAmount(), me, o));
        trackingRepo.save(new TrackingDetail(null, PLACED, o));
        return o;
    }

    private void fillItems(Order o, List<OrderItemRequest> reqs) {
        if (reqs == null || reqs.isEmpty()) throw ApiException.badRequest("Select at least one product");
        Map<Long, Integer> merged = new LinkedHashMap<>();
        for (OrderItemRequest r : reqs) {
            if (r == null || r.productId() == null || r.quantity() == null || r.quantity() < 1) {
                throw ApiException.badRequest("Each item needs a productId and a quantity of at least 1");
            }
            merged.merge(r.productId(), r.quantity(), Integer::sum);
        }
        o.getItems().clear();
        double total = 0;
        for (Map.Entry<Long, Integer> e : merged.entrySet()) {
            Product p = products.get(e.getKey());
            if (p.getStatus() != ProductStatus.ACTIVE) throw ApiException.badRequest(p.getName() + " is not available right now");
            o.getItems().add(new OrderItem(null, o, p, e.getValue(), p.getPrice()));
            total += p.getPrice() * e.getValue();
        }
        o.setOrderAmount(total);
    }

    public List<Order> all() { return repo.findAll(Sort.by(Sort.Direction.DESC, "orderNo")); }
    public List<Order> listFor(User me) { return admin(me) ? all() : repo.findByUserUserIdOrderByOrderNoDesc(me.getUserId()); }
    public Order get(Long id) { return repo.findById(id).orElseThrow(() -> ApiException.notFound("Order not found: " + id)); }
    public Order getFor(User me, Long id) {
        Order o = get(id);
        if (!admin(me) && !o.getUser().getUserId().equals(me.getUserId())) throw ApiException.forbidden("This order belongs to another user");
        return o;
    }

    /** Change delivery address, payment method and/or the products and quantities (only while ORDER_PLACED). */
    @Transactional
    public Order update(User me, Long id, OrderRequest r) {
        Order o = getFor(me, id);
        if (!admin(me) && !PLACED.equals(statusOf(o))) {
            throw ApiException.conflict("Only orders that are still ORDER_PLACED can be changed");
        }
        if (r.addressId() != null) o.setShippingAddress(addresses.getOwned(o.getUser(), r.addressId()));
        List<Payment> pays = paymentRepo.findByOrderOrderNo(id);
        if (r.items() != null && !r.items().isEmpty()) {
            fillItems(o, r.items());
            for (Payment p : pays) p.setAmount(o.getOrderAmount());
        }
        if (r.paymentMethod() != null && !r.paymentMethod().isBlank()) {
            for (Payment p : pays) p.setMethod(r.paymentMethod().trim());
        }
        paymentRepo.saveAll(pays);
        return repo.save(o);
    }

    /** Cancel/delete an order (customers only while ORDER_PLACED). */
    @Transactional
    public void delete(User me, Long id) {
        Order o = getFor(me, id);
        if (!admin(me) && !PLACED.equals(statusOf(o))) {
            throw ApiException.conflict("This order is already being processed and can no longer be cancelled");
        }
        trackingRepo.deleteByOrderOrderNo(id);
        paymentRepo.deleteByOrderOrderNo(id);
        repo.delete(o);
    }

    @Transactional
    public void setStatus(Long id, String status) {
        Order o = get(id);
        o.setStatus(status);
        repo.save(o);
    }
}
