package com.ecommerce.service;

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
public class CategoryService {
    private final CategoryRepository repo;

    public ProductCategory create(ProductCategory c) { c.setCategoryId(null); return repo.save(c); }
    public List<ProductCategory> all() { return repo.findAll(); }
    public ProductCategory get(Long id) { return repo.findById(id).orElseThrow(() -> ApiException.notFound("Category not found: " + id)); }

    @Transactional
    public ProductCategory update(Long id, ProductCategory in) {
        ProductCategory c = get(id);
        c.setName(in.getName());
        return repo.save(c);
    }
    public void delete(Long id) { repo.delete(get(id)); }
}
