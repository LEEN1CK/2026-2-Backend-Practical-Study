# DDD 4계층
  ### Presentation
    : 사용자의 요청을 받아 전달하는 역할 ex) Controller

  ### Application
    : 도메인 로직을 실행하는 순서를 조율 ex) Service

  ### Domain
    : 비즈니스 규칙과 핵심 로직을 담는 메인 ex) Order, Product

  ### Infrastructure
    : DB, JPA, 외부 API 등 기술적 세부사항을 실제 처리

# DIP
  원칙: 상위 모듈은 하위 구현체에 의존하지 않고, interface에 의존해야 한다.

  ### Port
    : domain 계층에서 필요한 기능(요구사항)을 정의하는 interface

  ### Adapter
    : infrastructure 계층에서 Port를 실제 기술로 구현

# Hexagonal Architecture
  : 순수한 Java로 만들어진 도메인 규칙을 중심에 두고, 외부 기술은 교체 가능한 플러그인처럼 독립적이게 만든다.
