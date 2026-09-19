package com.gdghongik.commerce.service;

import com.gdghongik.commerce.entity.Product;
import com.gdghongik.commerce.entity.SellingStatus;
import com.gdghongik.commerce.repository.ProductRepository;
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

    @Transactional
    public void decreaseStock(Long productId, int quantity) {
        Product product = findById(productId);
        product.decreaseStock(quantity);
        productRepository.save(product);
    }
}
