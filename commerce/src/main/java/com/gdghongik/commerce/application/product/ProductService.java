package com.gdghongik.commerce.application.product;

<<<<<<< HEAD:commerce/src/main/java/com/gdghongik/commerce/application/product/ProductService.java
import com.gdghongik.commerce.domain.common.Quantity;
import com.gdghongik.commerce.domain.product.Product;
import com.gdghongik.commerce.domain.product.ProductRepository;
import com.gdghongik.commerce.infrastructure.persistence.ProductJpaRepository;
=======
import com.gdghongik.commerce.entity.Product;
import com.gdghongik.commerce.entity.Quantity;
import com.gdghongik.commerce.repository.ProductRepository;
>>>>>>> 8dcae08bde7f15116e8e272b5e3fde7b6fff8234:commerce/src/main/java/com/gdghongik/commerce/service/ProductService.java
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다. id=" + productId));
    }

    @Transactional
    public Product create(String name, long price, int stock) {
        return productRepository.save(new Product(name, price, stock));
    }

<<<<<<< HEAD
    /**
     * 이제 이 메서드는 상품을 ID로 찾고, 상품의 재고를 줄이고, 변경된 상품 정보를 저장하는 작업을 하라고 명령합니다.
     * 재고를 줄여도 되는지에 대한 판단은 Product가 직접 합니다.
     */
    @Transactional
    public void decreaseStock(Long productId, Quantity quantity) {
=======
    @Transactional
    public void decreaseStock(Long productId, int quantity) {
>>>>>>> 54ae7e0cf574cdd4ee43ac01be83dccf403781e8
        Product product = findById(productId);
        product.decreaseStock(Quantity.of(quantity));
        productRepository.save(product);
    }
}
