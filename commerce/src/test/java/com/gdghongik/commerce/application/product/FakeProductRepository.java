package com.gdghongik.commerce.application.product;

import com.gdghongik.commerce.domain.product.Product;
import com.gdghongik.commerce.domain.product.ProductRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.test.util.ReflectionTestUtils;

public class FakeProductRepository implements ProductRepository {

    private final Map<Long, Product> store = new HashMap<>();
    private long sequence = 0L;

    @Override
    public Product save(Product product) {
        if (product.getId() == null) {
            ReflectionTestUtils.setField(product, "id", ++sequence);
        }
        store.put(product.getId(), product);
        return product;
    }

    @Override
    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Product> findByIdWithLock(Long id) {
        // ProductRepository에 추가된 메서드를 구현해 기존 단위 테스트를 유지한다.
        // Fake는 실제 DB 잠금 없이 일반 조회만 제공한다.
        // 잠금 동작은 별도의 DB 테스트에서 검증한다.
        return findById(id);
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(store.values());
    }
}
