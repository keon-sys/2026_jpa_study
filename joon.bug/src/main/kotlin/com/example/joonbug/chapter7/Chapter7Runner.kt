package com.example.joonbug.chapter7

import jakarta.persistence.EntityManagerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(8)
class Chapter7Runner(
    private val entityManagerFactory: EntityManagerFactory
) : CommandLineRunner {

    override fun run(vararg args: String) {
        println("===== Chapter 7: 고급 매핑 테스트 =====")

        val orderId = prepareData()
        verify1_MappedSuperclass()
        verify2_PolymorphicQuery()
        verify3_OrderItemLazyLoading(orderId)

        /*
         * 1. @MappedSuperclass를 엔티티 상속으로 처리하지 않은 이유는 무엇인가?
         * ㄴ 테이블이 필요하지 않고, 다형성 조회가 불필요함. 연관관계또 안필요함
         * 2. 왜 join 상속 전략을 선택?
         * ㄴ SINGLE_TABLE 사용하기엔 추가 타입 생성 시 마다 db 작업 부담스러움
         * ㄴ TABLE_PER_CLASS 맵도메인에서는 이거 사용하고 있지 않나..? 일단 책에서 비추천한대서 안씀
         * 3. 비식별 선택 이유
         * ㄴ 비즈니스 요구사항 변경에 유연한 대응
         * ㄴ 교통 데이터에서는 식별키로 사용하는 것도 괜찮은 전략이지 않을까
         * 4. 옵션 타입 컬렉션 선택에 대해 어떠한 위험 감수인지
         * ㄴ 옵션 수정 시 전체 delete / insert
         * ㄴ PK 없음
         * ㄴ N+1 문제
         * ㄴ 네이버나 쿠팡 옵션 선택은 자유도가 높을텐데 어떻게 하는걸까
         *   ㄴ option은 1,2,3 이런식으로 가고 name으로 표출하기..?
         */
    }

    private fun prepareData(): Long {
        val em = entityManagerFactory.createEntityManager()
        val tx = em.transaction
        var orderId: Long = 0

        try {
            tx.begin()

            // Item 2개 생성
            val normalItem = NormalItem().apply {
                name = "USB Cable"
                basePrice = 9900
                sku = "USB-001"
                stockQuantity = 100
            }
            em.persist(normalItem)

            val subscriptionItem = SubscriptionItem().apply {
                name = "Music Sub"
                basePrice = 7900
                billingCycleDays = 30
                trialDays = 7
            }
            em.persist(subscriptionItem)

            // Order 생성
            val order = Ch7Order().apply {
                orderNo = "ORD-20260204-0001"
            }
            em.persist(order)
            orderId = order.id!!

            // OrderItem 2개 생성
            val orderItem1 = OrderItem.create(
                item = normalItem,
                quantity = 2,
                options = listOf("color" to "black", "size" to "M")
            )
            order.addOrderItem(orderItem1)
            em.persist(orderItem1)

            val orderItem2 = OrderItem.create(
                item = subscriptionItem,
                quantity = 1,
                options = listOf("trial" to "true")
            )
            order.addOrderItem(orderItem2)
            em.persist(orderItem2)

            em.flush()
            tx.commit()
        } catch (e: Exception) {
            if (tx.isActive) tx.rollback()
            throw e
        } finally {
            em.close()
        }

        return orderId
    }

    private fun verify1_MappedSuperclass() {
        val em = entityManagerFactory.createEntityManager()

        try {
            println("----- 3-2. 검증 1: @MappedSuperclass 반영 확인 -----")

            // 테이블 목록 조회 (H2)
            val tables = em.createNativeQuery(
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'"
            ).resultList

            println("생성된 테이블 목록")
            tables.filter { it.toString().contains("ch7", ignoreCase = true) }
                .forEach { println("  - $it") }

            // created_at, updated_at 컬럼 확인
            println()
            println("ch7_item 테이블 컬럼")
            val itemColumns = em.createNativeQuery(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'CH7_ITEM'"
            ).resultList
            itemColumns.forEach { println("  - $it") }

            println()
            println("ch7_order 테이블 컬럼")
            val orderColumns = em.createNativeQuery(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'CH7_ORDER'"
            ).resultList
            orderColumns.forEach { println("  - $it") }


        } finally {
            em.close()
        }
    }

    private fun verify2_PolymorphicQuery() {
        val em = entityManagerFactory.createEntityManager()

        try {
            println("----- 3-3. 검증 2: 다형성 조회 SQL 확인 -----")

            val items = em.createQuery("select i from Item i order by i.id", Item::class.java)
                .resultList

            println("조회된 Item 목록")
            items.forEach { item ->
                when (item) {
                    is NormalItem -> println("  - [NormalItem] ${item.name}, sku=${item.sku}")
                    is SubscriptionItem -> println("  - [SubscriptionItem] ${item.name}, billingCycleDays=${item.billingCycleDays}")
                }
            }

            println()
            println("부모 + 모든 자식 LEFT JOIN, DTYPE으로 타입을 구분하기 때문에 단일쿼리로 타입에 따른 자식 조회가능")

        } finally {
            em.close()
        }
    }

    private fun verify3_OrderItemLazyLoading(orderId: Long) {
        val em = entityManagerFactory.createEntityManager()

        try {
            println("----- 3-4. 검증 3: Order 조회 시 LAZY 로딩 확인 -----")
            println()
            println("order 단건 조회")
            val order = em.find(Ch7Order::class.java, orderId)
            println("ch7_order 테이블만 조회")

            println()
            println("order.orderItems 접근 (LAZY 로딩 트리거)")
            val orderItems = order.orderItems
            println("ch7_order_item 테이블만 조회")

            println()
            println("각 OrderItem의 Item 접근 (LAZY 로딩 트리거)")
            println("각 Item 의 option 접근 (@ElementCollection 기본 Lazy 로딩 트리거")
            orderItems.forEach { oi ->
                println("${oi.itemNameSnapshot}: item.name = ${oi.item?.name}")
                oi.options.forEach { option -> println("${option.optionKey}: ${option.optionValue}") }
            }
            println("ch7_item left join ch7_normal_item left join ch7_subscription_item 조회")
            println("ch7_order_item_option 테이블만 조회")

        } finally {
            em.close()
        }
    }
}