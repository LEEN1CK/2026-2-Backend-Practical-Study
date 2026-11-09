package com.gdghongik.commerce.infrastructure.persistence;

import com.gdghongik.commerce.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
    // TODO[W5-1]: DISTINCT Fetch Join으로 주문과 주문 항목을 함께 조회하는 findAllWithItems() 메서드를 작성하세요.
    @Query("""
            select distinct o
            from Order o
            left join fetch o.orderItems
            """)
    List<Order> findAllWithItems();
}
