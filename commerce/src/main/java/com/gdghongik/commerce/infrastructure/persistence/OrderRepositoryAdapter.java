package com.gdghongik.commerce.infrastructure.persistence;

import com.gdghongik.commerce.domain.order.Order;
import com.gdghongik.commerce.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    @Override
    public Order save(Order order) {return jpaRepository.save(order);}

    @Override
    public Optional<Order> findById(long id) {return jpaRepository.findById(id);}

    @Override
    public List<Order> findAll() {return jpaRepository.findAll();}
}
