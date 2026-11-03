package com.gdghongik.commerce.application.product;

import com.gdghongik.commerce.domain.product.Product;
import com.gdghongik.commerce.domain.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/**
 * FakeProductRepository를 사용해 상품 서비스의 동작을 검증하는 단위 테스트입니다.
 * 실제 DB 반영과 트랜잭션 동작은 별도의 통합 테스트에서 검증합니다.
 */
class ProductServiceTest {

    private ProductService productService;
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository = new FakeProductRepository();
        productService = new ProductService(productRepository);
    }

    @Test
    @DisplayName("재고를 정상적으로 감소시킨다")
    void 재고를_정상적으로_감소시킨다() {
        // given - 재고가 10개인 상품이 있다
        Product product = productRepository.save(new Product("기계식 키보드", 129_000L, 10));

        // when - 3개를 주문한다
        productService.decreaseStock(product.getId(), 3);

        // then - 재고가 7개가 된다
        Product found = productRepository.findById(product.getId()).orElseThrow();
        assertThat(found.getStock()).isEqualTo(7);
    }

    @Test
    @DisplayName("수량이 0 이하이면 예외가 발생한다")
    void 수량이_0_이하이면_예외가_발생한다() {
        // given
        Product product = productRepository.save(new Product("기계식 키보드", 129_000L, 10));

        // when & then, 상품을 0개 사려고 시도할 때 적절한 예외를 반환하는지 확인합니다.
        assertThatThrownBy(() -> productService.decreaseStock(product.getId(), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("수량은 1개 이상이어야 합니다.");
    }

    @Test
    @DisplayName("재고보다 많이 주문하면 예외가 발생한다")
    void 재고보다_많이_주문하면_예외가_발생한다() {
        // given - 재고가 3개인 상품
        Product product = productRepository.save(new Product("무선 마우스", 45_000L, 3));

        // when & then, 재고가 3개인 상품을 4개 사려고 시도할 때 적절한 예외를 반환하는지 확인합니다.
        assertThatThrownBy(() -> productService.decreaseStock(product.getId(), 4))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("재고가 부족합니다");
    }

    @Test
    @DisplayName("판매 중이 아닌 상품은 재고를 줄일 수 없다")
    void 판매중이_아닌_상품은_재고를_줄일_수_없다() {
        // given - 판매가 중지된 상품
        Product product = new Product("단종된 USB 허브", 25_000L, 5);
        product.stopSelling();
        productRepository.save(product);

        // when & then, 상태가 STOPPED인 상품을 사려고 시도할 때 적절한 예외를 반환하는지 확인합니다.
        assertThatThrownBy(() -> productService.decreaseStock(product.getId(), 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("판매 중인 상품이 아닙니다.");
    }
}
