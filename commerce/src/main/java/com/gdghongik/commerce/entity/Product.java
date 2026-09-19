package com.gdghongik.commerce.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private long price;

    private int stock;

    @Enumerated(EnumType.STRING)
    private SellingStatus status;

    public Product(String name, long price, int stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.status = SellingStatus.SELLING;
    }

    public void stopSelling() {
        this.status = SellingStatus.STOPPED;
    }

    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }
        if (this.status != SellingStatus.SELLING) {
            throw new IllegalStateException("판매 중인 상품이 아닙니다.");
        }
        if (this.stock < quantity) {
            throw new IllegalStateException("재고가 부족합니다. 남은 재고=" + this.stock);
        }

        this.stock -= quantity;

        if (this.stock  == 0) {
            this.status = SellingStatus.SOLD_OUT;
        }
    }
}
