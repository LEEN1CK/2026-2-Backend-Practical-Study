package com.gdghongik.commerce.application.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.gdghongik.commerce.application.order.dto.CreateOrderCommand;
import com.gdghongik.commerce.domain.product.Product;
import com.gdghongik.commerce.domain.product.ProductRepository;
import com.gdghongik.commerce.infrastructure.persistence.OrderJpaRepository;
import com.gdghongik.commerce.infrastructure.persistence.ProductJpaRepository;
import com.gdghongik.commerce.infrastructure.persistence.ProductRepositoryAdapter;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(ConcurrentOrderTest.ConcurrentOrderTestConfiguration.class)
class ConcurrentOrderTest {

    private static final int INITIAL_STOCK = 5;
    private static final int ORDER_COUNT = 10;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderJpaRepository orderRepository;

    @Autowired
    private ProductJpaRepository productRepository;

    @Autowired
    private ConcurrentReadBarrier readBarrier;

    private Long productId;

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        if (productId != null) {
            productRepository.deleteById(productId);
        }
    }

    @Test
    @DisplayName("동시에 처리된 주문 수만큼 재고가 감소한다")
    void 동시에_처리된_주문_수만큼_재고가_감소한다() throws Exception {
        // given
        Product product = productRepository.save(
                new Product("기계식 키보드", 129_000L, INITIAL_STOCK));
        productId = product.getId();

        // 락이 없는 경우 두 트랜잭션이 같은 재고를 읽도록 맞춘다.
        // 비관적 락 조회에는 이 장치를 적용하지 않는다.
        readBarrier.arm(ORDER_COUNT);

        ExecutorService executor = Executors.newFixedThreadPool(ORDER_COUNT);
        CountDownLatch ready = new CountDownLatch(ORDER_COUNT);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(ORDER_COUNT);
        AtomicInteger successCount = new AtomicInteger();
        Queue<Throwable> failures = new ConcurrentLinkedQueue<>();

        try {
            for (int i = 0; i < ORDER_COUNT; i++) {
                executor.submit(() -> {
                    ready.countDown();
                    try {
                        start.await();
                        orderService.place(new CreateOrderCommand(productId, 1));
                        successCount.incrementAndGet();
                    } catch (Throwable throwable) {
                        failures.add(throwable);
                    } finally {
                        done.countDown();
                    }
                });
            }

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            assertThat(done.await(10, TimeUnit.SECONDS)).isTrue();
        } finally {
            executor.shutdownNow();
        }

        // then
        Product foundProduct = productRepository.findById(productId).orElseThrow();
        long savedOrderCount = orderRepository.count();
        int failureCount = failures.size();

        System.out.printf(
                "동시 주문 결과: 성공=%d, 실패=%d, 최종 재고=%d%n",
                successCount.get(), failureCount, foundProduct.getStock());
        failures.forEach(failure ->
                System.out.println("실패 예외: " + failure.getClass().getSimpleName()));

        assertThat(successCount.get() + failureCount).isEqualTo(ORDER_COUNT);
        assertThat(savedOrderCount).isEqualTo(successCount.get());
        assertThat(foundProduct.getStock())
                .as("성공한 주문 수만큼 재고가 감소해야 한다")
                .isEqualTo(INITIAL_STOCK - successCount.get());
    }

    @TestConfiguration
    static class ConcurrentOrderTestConfiguration {

        @Bean
        ConcurrentReadBarrier concurrentReadBarrier() {
            return new ConcurrentReadBarrier();
        }

        @Bean
        @Primary
        ProductRepository synchronizedProductRepository(
                ProductRepositoryAdapter delegate,
                ConcurrentReadBarrier readBarrier) {
            return new SynchronizedReadProductRepository(delegate, readBarrier);
        }
    }

    static class ConcurrentReadBarrier {

        private volatile CountDownLatch latch;

        void arm(int parties) {
            this.latch = new CountDownLatch(parties);
        }

        void awaitBothReads() {
            CountDownLatch current = this.latch;
            if (current == null) {
                return;
            }

            current.countDown();
            try {
                if (!current.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("두 주문의 상품 조회를 동기화하지 못했습니다.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("동시 주문 테스트가 중단되었습니다.", e);
            }
        }
    }

    static class SynchronizedReadProductRepository implements ProductRepository {

        private final ProductRepository delegate;
        private final ConcurrentReadBarrier readBarrier;

        SynchronizedReadProductRepository(
                ProductRepository delegate,
                ConcurrentReadBarrier readBarrier) {
            this.delegate = delegate;
            this.readBarrier = readBarrier;
        }

        @Override
        public Product save(Product product) {
            return delegate.save(product);
        }

        @Override
        public Optional<Product> findById(Long id) {
            Optional<Product> product = delegate.findById(id);
            readBarrier.awaitBothReads();
            return product;
        }

        @Override
        public List<Product> findAll() {
            return delegate.findAll();
        }

        @Override
        public Optional<Product> findByIdWithLock(Long id) {
            return delegate.findByIdWithLock(id);
        }
    }
}
