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
public class AddressService {
    private final AddressRepository repo;

    public List<Address> mine(User me) { return repo.findByUserUserId(me.getUserId()); }

    @Transactional
    public Address create(User me, Address a) {
        a.setAddressId(null);
        a.setUser(me);
        return repo.save(a);
    }
    public Address get(Long id) { return repo.findById(id).orElseThrow(() -> ApiException.notFound("Address not found: " + id)); }
    public Address getOwned(User me, Long id) {
        Address a = get(id);
        if (!a.getUser().getUserId().equals(me.getUserId())) throw ApiException.forbidden("This address belongs to another user");
        return a;
    }
    @Transactional
    public Address update(User me, Long id, Address in) {
        Address a = getOwned(me, id);
        a.setCity(in.getCity()); a.setState(in.getState()); a.setCountry(in.getCountry());
        return repo.save(a);
    }
    @Transactional
    public void delete(User me, Long id) { repo.delete(getOwned(me, id)); }
}
