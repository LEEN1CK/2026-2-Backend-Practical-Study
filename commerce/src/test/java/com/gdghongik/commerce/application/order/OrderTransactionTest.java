package com.gdghongik.commerce.application.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gdghongik.commerce.application.order.dto.CreateOrderCommand;
import com.gdghongik.commerce.domain.order.Order;
import com.gdghongik.commerce.domain.order.OrderStatus;
import com.gdghongik.commerce.domain.product.Product;
import com.gdghongik.commerce.infrastructure.persistence.OrderJpaRepository;
import com.gdghongik.commerce.infrastructure.persistence.ProductJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@ActiveProfiles("test")
class OrderTransactionTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderJpaRepository orderRepository;

    @Autowired
    private ProductJpaRepository productRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Long productId;
    private Long orderId;

    @AfterEach
    void tearDown() {
        // 서비스의 커밋을 확인하므로 자동 롤백 대신 이번 테스트의 데이터만 정리한다.
        if (orderId != null) {
            orderRepository.deleteById(orderId);
        }
        if (productId != null) {
            productRepository.deleteById(productId);
        }
    }

    @Test
    @DisplayName("상품을 주문하면 DB에 주문이 저장되고 재고가 감소한다")
    void 주문이_저장되고_재고가_감소한다() {
        // given
        Product product = productRepository.save(new Product("기계식 키보드", 129_000L, 10));
        productId = product.getId();

        // when
        orderId = orderService.place(new CreateOrderCommand(productId, 1));

        // then
        // 테스트에 @Transactional을 붙이지 않고, 서비스 커밋 후 DB에서 다시 조회한다.
        Order foundOrder = orderRepository.findById(orderId).orElseThrow();
        Product foundProduct = productRepository.findById(productId).orElseThrow();
        assertThat(foundOrder.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(foundProduct.getStock()).isEqualTo(9);
    }

    @Test
    @DisplayName("주문 처리 중 예외가 발생하면 주문과 재고 변경이 함께 롤백된다")
    void 예외가_발생하면_주문과_재고가_함께_롤백된다() {
        // given
        Product product = productRepository.save(new Product("기계식 키보드", 129_000L, 10));
        productId = product.getId();
        long orderCount = orderRepository.count();

        // when
        assertThatThrownBy(() -> 주문_처리_후_예외_발생(orderCount))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("롤백 확인용 예외");

        // then
        Product found = productRepository.findById(productId).orElseThrow();
        assertThat(found.getStock()).isEqualTo(10);
        assertThat(orderRepository.count()).isEqualTo(orderCount);
    }

    private void 주문_처리_후_예외_발생(long orderCount) {
        // 서비스가 이 트랜잭션에 참여하게 해, 운영 코드 수정 없이 처리 중 실패를 재현한다.
        transactionTemplate.executeWithoutResult(status -> {
            orderService.place(new CreateOrderCommand(productId, 1));
            orderRepository.flush();

            // 차감 자체가 빠져 있는데 롤백 테스트가 통과하는 것을 방지한다.
            Product changed = productRepository.findById(productId).orElseThrow();
            assertThat(changed.getStock()).isEqualTo(9);
            assertThat(orderRepository.count()).isEqualTo(orderCount + 1);
            throw new IllegalStateException("롤백 확인용 예외");
        });
    }
}
