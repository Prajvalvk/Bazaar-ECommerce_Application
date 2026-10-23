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

    @Transactional
    public Product create(Product p) {
        p.setProductId(null);
        p.setStatus(p.getStatus());
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
        p.setStatus(in.getStatus());
        p.setCategory(resolveCategory(in.getCategory()));
        return repo.save(p);
    }
    @Transactional
    public Product setStatus(Long id, ProductStatus status) {
        Product p = get(id);
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
