package homework.chapter_7

import jakarta.persistence.*

@Entity(name = "Ch7OrderItemKt")
@Table(name = "order_item_ch7_kt")
class OrderItem( // 2-3. 비식별 관계(A) / 식별 관계-복합키(B) 중 비식별 관계 선택
    @Id
    @GeneratedValue
    @Column(name = "order_item_id")
    val id: Long? = null,  // 자체 PK (비식별)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    var order: Order? = null,  // 소유자(부모) — OrderItem은 Order 없이 단독으로 존재할 수 없다 (cascade=ALL, orphanRemoval=true)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    var item: Item? = null,  // 참조 대상 — Item 삭제해도 OrderItem 유지, 가격/이름 변경 대비 스냅샷 필요

    // OrderItem은 주문 시점의 정보(가격/옵션/수량)를 반드시 보존해야 한다.
    @Column(nullable = false)
    var itemNameSnapshot: String = "", // 주문 시점의 상품명 스냅샷

    @Column(nullable = false)
    var unitPriceSnapshot: Long = 0, // 주문 시점의 단가 스냅샷

    @Column(nullable = false)
    var quantity: Int = 0,

    @ElementCollection
    @CollectionTable(
        name = "order_item_option_ch7_kt",
        joinColumns = [JoinColumn(name = "order_item_id")]
    )
    val options: MutableSet<Option> = mutableSetOf() // 주문 시점의 옵션
) : BaseEntity()
