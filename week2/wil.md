# DDD
    : Domain-Driven Design
    - 비즈니스 로직과 도메인 개념에 집중하는 설계 방식
    - 단순 상태 변경 메서드 대신에 도메인의 의미가 명확히 드러나는 메서드를 사용

# Value Object
    - 단순 원시 타입(private long price)으로 다루면 도메인 로직이 Service 레이어로 흩어지기 쉬움
    - 객체(private Money price)로 캡슐화하여 비즈니스 로직을 객체 내부로 모음

# Agrgregate
    Aggregate : 관련된 도메인 객체들을 하나로 묶은 데이터 변경의 단위
    Aggregate Root : Aggregate 외부와 소통하는 대표 엔티티

# 불변식
    : 객체가 살아 있는 동안 계속 참이어야 하는 조건

    - Aggregate를 정의하고 경계를 결정짓는 핵심 요소
    - 정적 팩토리 메서드(Static Factory Method)를 통해 불변식을 검증하여 올바른 객체만 생성되도록 함
