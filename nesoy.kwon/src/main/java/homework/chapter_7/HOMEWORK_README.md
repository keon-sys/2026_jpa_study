## 📘 7장 과제 – 고급 매핑

### 과제명
**주문상품(OrderItem) 모델링 결정과 검증 (MappedSuperclass 포함)**

---

### 목표
- 상속 매핑, `@MappedSuperclass`, 식별/비식별 관계, 값 타입 설계를 종합적으로 활용한다.
- “엔티티 상속(다형성 조회)”과 “공통 필드 추출(@MappedSuperclass)”의 차이를 명확히 구분한다.
- 구현 후 반드시 **SQL 로그로 검증**한다.

---

## 0) 과제 규칙
- 연관관계 기본은 `LAZY`를 권장

---

## 1) 도메인 시나리오

### 1-1. 공통 필드 요구사항
모든 엔티티는 생성/수정 시간 컬럼이 있어야 한다.
- `createdAt`, `updatedAt`
- 이 공통 필드는 `@MappedSuperclass`로 분리해야 하며 **테이블이 생성되면 안 된다.**

---

### 1-2. 상품(Item)은 3가지 타입을 가진다
부모: `Item`

서브 타입(총 2개):
1) `NormalItem`
- `sku` (String, not null)
- `stockQuantity` (int, not null)

2) `SubscriptionItem`
- `billingCycleDays` (int, not null)  // 예: 30일
- `trialDays` (int, not null)         // 예: 7일

공통(Item 부모)에 들어갈 필드(고정):
- `id` (PK)
- `name` (String, not null)
- `basePrice` (long, not null)

---

### 1-3. 주문 도메인
- `Order`는 1개 이상의 `OrderItem`을 가진다.
- `OrderItem`은 주문 시점의 정보(가격/옵션/수량)를 반드시 보존해야 한다.
- `OrderItem`은 `Order` 없이 단독으로 존재할 수 없다.

Order 필드(최소 고정):
- `id` (PK)
- `orderNo` (String, unique, not null)  // 예: ORD-20260204-0001

OrderItem 필드(최소 고정):
- 주문 당시 `itemNameSnapshot` (String, not null)  // Item.name 복사본
- 주문 당시 `unitPriceSnapshot` (long, not null)   // Item.basePrice 복사본
- `quantity` (int, not null)

> ⚠️ `OrderItem`과 `Item`의 연관관계
> - `OrderItem`이 `Item`을 **연관관계로 참조해도 되고(권장)**,
> - **참조하지 않아도 된다.**
> - 단, **주문 시점 스냅샷 필드는 반드시 포함**되어야 한다.

---

## 2) 구현 요구사항

### 2-1. `@MappedSuperclass`
- `BaseEntity` 같은 이름으로 구현(이름은 자유)
- `createdAt`, `updatedAt` 포함
- `@PrePersist`, `@PreUpdate`로 자동 세팅(권장)

---

### 2-2. 상속 매핑 전략
`Item` 상속 전략은 아래 중 **하나만 선택**해서 구현한다.
- `SINGLE_TABLE`
- `JOINED`
- `TABLE_PER_CLASS`

---

### 2-3. Order – OrderItem 관계
아래 둘 중 하나로 구현한다.

A) 비식별 관계
- `OrderItem`이 별도 PK(Long id)를 갖고
- `OrderItem`이 `order_id` FK로 `Order`를 참조한다.

B) 식별 관계(복합키)
- `OrderItem`의 PK를 `(order_id, item_id)` 복합키로 구현한다.
- `@EmbeddedId` + `@MapsId` 사용 권장

---

### 2-4. 옵션(Options) 설계
OrderItem의 옵션은 아래 중 하나로 설계한다.

A) 값 타입(`@Embeddable`) + 값 타입 컬렉션(`@ElementCollection`)
- 예: `Option(key, value)`를 값 타입으로 만들고 `Set<Option>` 형태로 보관

B) 옵션 엔티티 분리
- 예: `OrderItemOption` 엔티티(`order_item_id` FK)로 분리

옵션 데이터 규격(고정 예시):
- `color=black`
- `size=M`
- `giftWrap=true`

---

## 3) 필수 테스트 시나리오
아래 시나리오는 테스트 코드(또는 실행 코드)로 반드시 포함한다.

### 3-1. 데이터 준비
아래 데이터는 반드시 생성한다.

- Items 2개 생성 및 저장
    - NormalItem(name="USB Cable", basePrice=9900, sku="USB-001", stockQuantity=100)
    - SubscriptionItem(name="Music Sub", basePrice=7900, billingCycleDays=30, trialDays=7)

- Order 1개 생성 및 저장
    - orderNo="ORD-20260204-0001"

- OrderItem 2개 생성 (주문에 포함)
    - (NormalItem) quantity=2, options: color=black, size=M
    - (SubscriptionItem) quantity=1, options: trial=true


---

### 3-2. 검증 1: `@MappedSuperclass` 반영 확인
- 실제 생성된 테이블에 `created_at`, `updated_at`(또는 본인 네이밍)이 들어갔는지 확인
- `BaseEntity` 자체 테이블이 생기지 않았는지 확인

**제출에 포함할 것**
- DDL 또는 테이블 구조 캡처(또는 로그)
```
create table Item (
        basePrice bigint not null,
        createdAt timestamp(6),
        id bigint not null,
        updatedAt timestamp(6),
        name varchar(255) not null,
        primary key (id)
    )
```
---

### 3-3. 검증 2: 부모 타입(Item) 기준 다형성 조회 SQL 확인
아래 JPQL을 실행하고 SQL을 제출한다.

```java
select i from Item i order by i.id
```
#### JOINED
- 여러개의 테이블들을 LEFT JOIN
```
/* select
        i 
    from
        Item i 
    order by
        i.id */ select
            i1_0.id,
            case 
                when i1_1.id is not null 
                    then 1 
                when i1_2.id is not null 
                    then 2 
            end,
            i1_0.basePrice,
            i1_0.createdAt,
            i1_0.name,
            i1_0.updatedAt,
            i1_1.sku,
            i1_1.stockQuantity,
            i1_2.billingCycleDays,
            i1_2.trialDays 
        from
            Item i1_0 
        left join
            NormalItem i1_1 
                on i1_0.id=i1_1.id 
        left join
            SubscriptionItem i1_2 
                on i1_0.id=i1_2.id 
        order by
            i1_0.id
```

#### SINGLE_TABLE
- 하나의 테이블로 관리
```
    /* select
        i 
    from
        Item i 
    order by
        i.id */ select
            i1_0.id,
            i1_0.DTYPE,
            i1_0.basePrice,
            i1_0.createdAt,
            i1_0.name,
            i1_0.updatedAt,
            i1_0.sku,
            i1_0.stockQuantity,
            i1_0.billingCycleDays,
            i1_0.trialDays 
        from
            Item i1_0 
        order by
            i1_0.id
```

### 3-4. 검증 3: 주문 1건 조회 시 OrderItem / Item 조회 방식 확인

1. Order 단건 조회
```java
Order o = em.find(Order.class, orderId);
```
2. o.getOrderItems() 접근 시점의 SQL 확인

#### 단건
```
select
        o1_0.id,
        o1_0.createdAt,
        o1_0.orderNo,
        o1_0.updatedAt 
    from
        ORDERS o1_0 
    where
        o1_0.id=?
```

#### 여러건
```
select
        oi1_0.ORDER_ID,
        oi1_0.id,
        oi1_0.createdAt,
        oi1_0.itemNameSnapshot,
        oi1_0.quantity,
        oi1_0.unitPriceSnapshot,
        oi1_0.updatedAt 
    from
        OrderItem비식별관계 oi1_0 
    where
        oi1_0.ORDER_ID=?
```


## 과제를 하며 생각해볼 점들 -> 발표자가 이야기 해보아요!
- @MappedSuperclass를 엔티티 상속으로 처리하지 않은 이유는 무엇인가?
  - 별도의 엔티티 테이블이 생겨나고, 불필요한 JOIN이 발생한다.
- 왜 이 상속 전략을 선택했는가?
  - 단일 전략 - 테이블이 깔끔함, null 허용해야 함.
  - 조인 전략 - 테이블이 경량화, null x, 조인 많음. 
- 왜 식별 / 비식별 관계 중 이 방식을 선택했는가?
  - 식별 : 복합키를 사용하지만, 요구사항이 복잡해질 수록 어려워짐
  - 비식별 : 자체 키가 있기 때문에, 같은 상품을 여러번 추가할 수 있음
- 옵션을 값 타입 컬렉션으로 했다면, 어떤 위험을 감수한 것인가?
  - 값 수정시 전체 값을 수정해야 함.
- 옵션을 엔티티로 분리했다면, 어떤 장점과 비용이 생기는가?
  - 수정 비용이 적게 들지만, 테이블을 별도 관리해야 함. 