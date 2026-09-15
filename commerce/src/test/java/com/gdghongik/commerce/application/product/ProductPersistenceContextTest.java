package com.gdghongik.commerce.application.product;

import static org.assertj.core.api.Assertions.assertThat;

import com.gdghongik.commerce.domain.product.Product;
import com.gdghongik.commerce.infrastructure.persistence.ProductJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ProductPersistenceContextTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductJpaRepository productRepository;

    private Long productId;

    @AfterEach
    void tearDown() {
        // 테스트 전체를 롤백하지 않으므로, 이번 테스트에서 만든 상품만 정리한다.
        if (productId != null) {
            productRepository.deleteById(productId);
        }
    }

    @Test
    @DisplayName("서비스에서 차감한 재고가 트랜잭션 종료 후 DB에 반영된다")
    void 재고_차감이_트랜잭션_종료_후_DB에_반영된다() {
        // given
        Product product = productRepository.save(new Product("기계식 키보드", 129_000L, 10));
        productId = product.getId();

        // when
        // 서비스 자체의 커밋을 확인하기 위해 테스트에는 @Transactional을 붙이지 않는다.
        productService.decreaseStock(productId, 1);

        // then
        // 기존 객체의 값이 아니라, 서비스 트랜잭션 종료 후 DB에서 다시 읽은 값을 검증한다.
        Product found = productRepository.findById(productId).orElseThrow();
        assertThat(found.getStock()).isEqualTo(9);
    }
}
