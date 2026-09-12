package com.gdghongik.commerce.infrastructure.persistence;

import com.gdghongik.commerce.domain.product.Product;
import com.gdghongik.commerce.domain.product.ProductRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository jpaRepository;

    @Override
    public Product save(Product product) {
        return jpaRepository.save(product);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Product> findByIdWithLock(Long id) {
        // TODO[W4-3]: ProductJpaRepository의 잠금 조회 메서드에 위임하세요.
        return jpaRepository.findByIdWithLock(id);
    }

    @Override
    public List<Product> findAll() {
        return jpaRepository.findAll();
    }
}
