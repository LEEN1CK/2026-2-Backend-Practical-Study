package com.gdghongik.commerce.application.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.gdghongik.commerce.application.order.dto.OrderResult;
import com.gdghongik.commerce.domain.common.Money;
import com.gdghongik.commerce.domain.common.Quantity;
import com.gdghongik.commerce.domain.order.Order;
import com.gdghongik.commerce.domain.order.OrderItem;
import com.gdghongik.commerce.infrastructure.persistence.OrderJpaRepository;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.stream.IntStream;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:order-query-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.jpa.properties.hibernate.generate_statistics=true",
        "spring.jpa.properties.hibernate.cache.use_second_level_cache=false",
        "spring.jpa.properties.hibernate.cache.use_query_cache=false",
        "logging.level.org.hibernate.engine.internal.StatisticalLoggingSessionEventListener=OFF"
})
@ActiveProfiles("test")
class OrderQueryTest {

    private static final int ORDER_COUNT = 10;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderJpaRepository orderRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Statistics statistics;

    @BeforeEach
    void setUp() {
        statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();

        transactionTemplate.executeWithoutResult(status -> {
            for (int i = 1; i <= ORDER_COUNT; i++) {
                Order order = Order.place(OrderItem.create(
                        1L, "노트", Money.of(1_000L * i), Quantity.of(1)));
                order.addItem(OrderItem.create(
                        2L, "펜", Money.of(2_000L), Quantity.of(2)));
                order.addItem(OrderItem.create(
                        3L, "파일", Money.of(3_000L), Quantity.of(3)));
                orderRepository.save(order);
            }
        });
    }

    @AfterEach
    void tearDown() {
        transactionTemplate.executeWithoutResult(status -> orderRepository.deleteAll());
    }

    @Test
    @DisplayName("주문 목록의 주문 수와 각 주문의 총액이 정확하다")
    void 주문_목록의_주문_수와_총액이_정확하다() {
        List<OrderResult> results = orderService.findAll();

        assertThat(results).hasSize(ORDER_COUNT);
        assertThat(results).extracting(OrderResult::orderId).doesNotHaveDuplicates();
        Long[] expectedAmounts = IntStream.rangeClosed(1, ORDER_COUNT)
                .mapToObj(i -> 1_000L * i + 2_000L * 2 + 3_000L * 3)
                .toArray(Long[]::new);
        assertThat(results).extracting(OrderResult::totalAmount)
                .containsExactlyInAnyOrder(expectedAmounts);

        System.out.printf("주문 목록 결과: 주문=%d건, 각 주문의 총액 검증 완료%n", results.size());
    }

    @Test
    @DisplayName("주문 목록과 총액을 한 번의 조회 SQL로 가져온다")
    void 주문_목록과_총액을_한_번의_SQL로_조회한다() {

        statistics.clear();
        System.out.println("=== 주문 목록 조회 시작 (측정 구간) ===");
        List<OrderResult> results = orderService.findAll();
        long queryCount = statistics.getPrepareStatementCount();
        System.out.println("=== 주문 목록 조회 종료 (측정 구간) ===");

        System.out.printf("주문 목록 조회 결과: 주문=%d건, 조회 SQL=%d개%n", results.size(), queryCount);
        assertThat(results).hasSize(ORDER_COUNT);

        assertThat(queryCount)
                .as("주문과 주문 항목을 함께 가져와 추가 조회가 없어야 한다")
                .isEqualTo(1L);
    }

    @Test
    @DisplayName("Batch Size로 주문 항목을 묶어서 조회한다")
    void Batch_Size로_주문_항목을_묶어서_조회한다() {

        statistics.clear();
        System.out.println("=== Batch Size 주문 목록 조회 시작 (측정 구간) ===");
        List<OrderResult> results = transactionTemplate.execute(status ->
                orderRepository.findAll().stream()
                        .map(OrderResult::from)
                        .toList());
        long queryCount = statistics.getPrepareStatementCount();
        System.out.println("=== Batch Size 주문 목록 조회 종료 (측정 구간) ===");

        System.out.printf("Batch Size 조회 결과: 주문=%d건, 조회 SQL=%d개%n", results.size(), queryCount);
        assertThat(results).hasSize(ORDER_COUNT);

        assertThat(queryCount)
                .as("주문 목록 1개와 주문 항목 Batch 조회 1개가 실행되어야 한다")
                .isEqualTo(2L);
    }
}
