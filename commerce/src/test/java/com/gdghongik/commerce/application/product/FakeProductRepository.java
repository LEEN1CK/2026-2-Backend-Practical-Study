package com.gdghongik.commerce.application.product;

import com.gdghongik.commerce.domain.product.Product;
import com.gdghongik.commerce.domain.product.ProductRepository;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

// TODO[W3-4]: ProductRepository 를 구현하는 가짜 저장소를 만드세요.
//             HashMap 에 담고, id 가 없으면 하나씩 늘려서 붙이면 됩니다.
//             id 부여는 ReflectionTestUtils.setField(product, "id", ++sequence) 를 쓰세요.
//               (Product 에 setId 가 없어서 리플렉션으로 넣습니다. 테스트에서만 쓰는 방법입니다.)
public class FakeProductRepository implements ProductRepository{

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
    public Optional<Product> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(store.values());
    }
}
