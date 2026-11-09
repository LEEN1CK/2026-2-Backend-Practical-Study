package com.gdghongik.commerce.infrastructure.persistence;

import com.gdghongik.commerce.domain.order.Order;
import com.gdghongik.commerce.domain.order.OrderRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    @Override
    public Order save(Order order) {
        return jpaRepository.save(order);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Order> findAll() {

        // TODO[W5-2]: 주문 목록 조회가 findAllWithItems()를 사용하도록 변경하세요.
        return jpaRepository.findAllWithItems();
    }
}
