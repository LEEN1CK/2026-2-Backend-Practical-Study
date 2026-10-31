package com.gdghongik.commerce.infrastructure.persistence;

import com.gdghongik.commerce.domain.product.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {
    // TODO[W4-2]: 같은 상품을 변경하는 다른 트랜잭션이 기다리도록 비관적 쓰기 잠금을 사용하는 findByIdWithLock(Long id) 조회 메서드를 작성하세요.
    // 반환 타입은 Optional<Product>입니다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);
}