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
public class ProductService {
    private final ProductRepository repo;
    private final CategoryService categories;
    private final CartItemRepository cartItems;

    private static boolean admin(User u) { return u != null && u.getRole() == Role.ADMIN; }
    private static boolean visible(Product p) { return p.getStatus() != ProductStatus.INACTIVE; }
    private static void normalizeStatus(Product p) {
        ProductStatus current = p.getStatus();
        if (current == ProductStatus.INACTIVE) return;
        if (p.getQuantity() == null || p.getQuantity() < 1) {
            p.setStatus(ProductStatus.OUT_OF_STOCK);
        }
    }

    @Transactional
    public Product create(Product p) {
        p.setProductId(null);
        p.setStatus(p.getStatus());
        if (p.getQuantity() == null) p.setQuantity(0);
        if (p.getQuantityUnit() == null || p.getQuantityUnit().trim().isEmpty()) p.setQuantityUnit("Pieces");
        if (p.getImageUrl() != null && p.getImageUrl().trim().isEmpty()) p.setImageUrl(null);
        normalizeStatus(p);
        p.setCategory(resolveCategory(p.getCategory()));
        return repo.save(p);
    }
    public List<Product> all() { return repo.findAll(Sort.by("productId")); }
    public List<Product> listFor(User me) {
        return all().stream().filter(p -> admin(me) || visible(p)).collect(Collectors.toList());
    }
    public Product get(Long id) { return repo.findById(id).orElseThrow(() -> ApiException.notFound("Product not found: " + id)); }
    public Product getFor(User me, Long id) {
        Product p = get(id);
        if (!admin(me) && !visible(p)) throw ApiException.notFound("Product not found: " + id);
        return p;
    }
    public List<Product> byCategoryFor(User me, Long categoryId) {
        categories.get(categoryId);
        return repo.findByCategoryCategoryId(categoryId).stream()
                .filter(p -> admin(me) || visible(p)).collect(Collectors.toList());
    }

    @Transactional
    public Product update(Long id, Product in) {
        Product p = get(id);
        p.setName(in.getName()); p.setPrice(in.getPrice()); p.setDescription(in.getDescription());
        p.setQuantity(in.getQuantity() == null ? 0 : in.getQuantity());
        p.setQuantityUnit(in.getQuantityUnit() == null || in.getQuantityUnit().trim().isEmpty() ? "Pieces" : in.getQuantityUnit().trim());
        p.setStatus(in.getStatus());
        p.setImageUrl(in.getImageUrl() == null || in.getImageUrl().trim().isEmpty() ? null : in.getImageUrl().trim());
        normalizeStatus(p);
        p.setCategory(resolveCategory(in.getCategory()));
        return repo.save(p);
    }
    @Transactional
    public Product setStatus(Long id, ProductStatus status) {
        Product p = get(id);
        if (status == ProductStatus.ACTIVE && (p.getQuantity() == null || p.getQuantity() < 1)) {
            throw ApiException.badRequest("Set the quantity above 0 before marking this product Active");
        }
        p.setStatus(status);
        return repo.save(p);
    }
    @Transactional
    public void delete(Long id) {
        Product p = get(id);
        cartItems.deleteByProductProductId(id);
        repo.delete(p);
    }

    private ProductCategory resolveCategory(ProductCategory ref) {
        if (ref == null) return null;
        if (ref.getCategoryId() == null) throw ApiException.badRequest("category.categoryId is required");
        return categories.get(ref.getCategoryId());
    }
}
