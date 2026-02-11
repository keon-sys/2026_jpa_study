package com.example.joonbug.chapter7

import jakarta.persistence.*
import java.time.LocalDateTime

@MappedSuperclass
abstract class BaseEntity {
    @Column(name = "created_at")
    var createdAt: LocalDateTime? = null
        protected set

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
        protected set

    @PrePersist
    fun prePersist() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

@Entity
@Table(name = "ch7_item")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "DTYPE")
abstract class Item : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var name: String = ""
    @Column(nullable = false)
    var basePrice: Long = 0
}

@Entity
@Table(name = "ch7_normal_item")
@DiscriminatorValue("NORMAL")
class NormalItem : Item() {
    @Column(nullable = false)
    var sku: String = ""

    @Column(nullable = false)
    var stockQuantity: Int = 0
}

@Entity
@Table(name = "ch7_subscription_item")
@DiscriminatorValue("SUBSCRIPTION")
class SubscriptionItem : Item() {
    @Column(nullable = false)
    var billingCycleDays: Int = 0

    @Column(nullable = false)
    var trialDays: Int = 0
}

@Embeddable
data class OrderItemOption(
    @Column(name = "option_key", nullable = false)
    val optionKey: String = "",

    @Column(name = "option_value", nullable = false)
    val optionValue: String = ""
)

@Entity
@Table(name = "ch7_order")
class Ch7Order : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, unique = true)
    var orderNo: String = ""

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    val orderItems: MutableList<OrderItem> = mutableListOf()

    fun addOrderItem(orderItem: OrderItem) {
        orderItems.add(orderItem)
        orderItem.order = this
    }
}

@Entity
@Table(name = "ch7_order_item")
class OrderItem : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: Ch7Order? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    var item: Item? = null

    // 주문 시점 스냅샷
    @Column(name = "item_name_snapshot", nullable = false)
    var itemNameSnapshot: String = ""

    @Column(name = "unit_price_snapshot", nullable = false)
    var unitPriceSnapshot: Long = 0

    @Column(nullable = false)
    var quantity: Int = 0

    // 옵션: 값 타입 컬렉션
    @ElementCollection
    @CollectionTable(
        name = "ch7_order_item_option",
        joinColumns = [JoinColumn(name = "order_item_id")]
    )
    val options: MutableSet<OrderItemOption> = mutableSetOf()

    companion object {
        fun create(
            item: Item,
            quantity: Int,
            options: List<Pair<String, String>> = emptyList()
        ): OrderItem {
            return OrderItem().apply {
                this.item = item
                this.itemNameSnapshot = item.name
                this.unitPriceSnapshot = item.basePrice
                this.quantity = quantity
                options.forEach { (key, value) ->
                    this.options.add(OrderItemOption(key, value))
                }
            }
        }
    }
}