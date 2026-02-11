package homework.chapter_7

import jakarta.persistence.*

@Entity(name = "Ch7OrderKt")
@Table(name = "orders_ch7_kt")
class Order(
    @Id
    @GeneratedValue
    @Column(name = "order_id")
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    var orderNo: String = "",

    // Order는 1개 이상의 OrderItem을 가진다.
    @OneToMany(mappedBy = "order",
        cascade = [CascadeType.ALL], // Order를 persist하면 OrderItem도 자동 저장
        orphanRemoval = true) // orderItems에서 제거하면 DB에서도 삭제
    val orderItems: MutableList<OrderItem> = mutableListOf()
) : BaseEntity()
