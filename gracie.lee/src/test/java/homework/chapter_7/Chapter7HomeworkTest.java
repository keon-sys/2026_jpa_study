package homework.chapter_7;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 7장 과제: 주문상품(OrderItem) 모델링 검증
 */
class Chapter7HomeworkTest {

    private static EntityManagerFactory emf;
    private EntityManager em;
    private EntityTransaction tx;

    @BeforeAll
    static void setUpFactory() {
        emf = Persistence.createEntityManagerFactory("jpabook");
    }

    @AfterAll
    static void closeFactory() {
        if (emf != null) emf.close();
    }

    @BeforeEach
    void setUp() {
        em = emf.createEntityManager();
        tx = em.getTransaction();
        tx.begin();
    }

    @AfterEach
    void tearDown() {
        if (tx.isActive()) tx.rollback();
        if (em != null) em.close();
    }

    // ========== 3-1. 데이터 준비 ==========

    @Test
    @DisplayName("3-1: 데이터 준비 - Items, Order, OrderItem 생성 및 저장")
    void 데이터_준비() {
        // 1. NormalItem 생성
        NormalItem normalItem = new NormalItem();
        normalItem.setName("USB Cable");
        normalItem.setBasePrice(9900);
        normalItem.setSku("USB-001");
        normalItem.setStockQuantity(100);
        em.persist(normalItem);

        // 2. SubscriptionItem 생성
        SubscriptionItem subItem = new SubscriptionItem();
        subItem.setName("Music Sub");
        subItem.setBasePrice(7900);
        subItem.setBillingCycleDays(30);
        subItem.setTrialDays(7);
        em.persist(subItem);

        // 3. Order 생성
        Order order = new Order();
        order.setOrderNo("ORD-20260204-0001");
        em.persist(order);

        // 4. OrderItem 1 (NormalItem)
        OrderItem orderItem1 = new OrderItem();
        orderItem1.setOrder(order);
        orderItem1.setItem(normalItem);
        orderItem1.setItemNameSnapshot("USB Cable");
        orderItem1.setUnitPriceSnapshot(9900);
        orderItem1.setQuantity(2);
        orderItem1.getOptions().add(new Option("color", "black"));
        orderItem1.getOptions().add(new Option("size", "M"));
        order.getOrderItems().add(orderItem1);

        // 5. OrderItem 2 (SubscriptionItem)
        OrderItem orderItem2 = new OrderItem();
        orderItem2.setOrder(order);
        orderItem2.setItem(subItem);
        orderItem2.setItemNameSnapshot("Music Sub");
        orderItem2.setUnitPriceSnapshot(7900);
        orderItem2.setQuantity(1);
        orderItem2.getOptions().add(new Option("trial", "true"));
        order.getOrderItems().add(orderItem2);

        em.flush();
        em.clear();
        System.out.println("===== 데이터 준비 완료 =====");

        // 검증
        Order found = em.find(Order.class, order.getId());
        assertNotNull(found);
        assertEquals(2, found.getOrderItems().size());
    }

    // ========== 3-2. 검증 1: @MappedSuperclass 반영 확인 ==========

    @Test
    @DisplayName("3-2: BaseEntity 테이블이 생성되지 않고, created_at/updated_at이 각 테이블에 존재하는지 확인")
    void MappedSuperclass_확인() {
        NormalItem normalItem = new NormalItem();
        normalItem.setName("Test Item");
        normalItem.setBasePrice(1000);
        normalItem.setSku("TEST-001");
        normalItem.setStockQuantity(10);
        em.persist(normalItem);

        em.flush();
        em.clear();

        // created_at, updated_at이 자동 세팅되었는지 확인
        NormalItem found = em.find(NormalItem.class, normalItem.getId());
        assertNotNull(found.getCreatedAt(), "createdAt이 @PrePersist로 세팅되어야 함");
        assertNotNull(found.getUpdatedAt(), "updatedAt이 @PrePersist로 세팅되어야 함");

        System.out.println("===== @MappedSuperclass 검증 =====");
        System.out.println("createdAt = " + found.getCreatedAt());
        System.out.println("updatedAt = " + found.getUpdatedAt());
        System.out.println("BaseEntity 테이블은 DDL 로그에서 CREATE TABLE base_entity가 없어야 함");
    }

    // ========== 3-3. 검증 2: 부모 타입(Item) 다형성 조회 SQL 확인 ==========

    @Test
    @DisplayName("3-3: select i from Item i order by i.id → JOINED 전략 SQL 확인")
    void 다형성_조회_SQL_확인() {
        // 데이터 준비
        NormalItem normalItem = new NormalItem();
        normalItem.setName("USB Cable");
        normalItem.setBasePrice(9900);
        normalItem.setSku("USB-001");
        normalItem.setStockQuantity(100);
        em.persist(normalItem);

        SubscriptionItem subItem = new SubscriptionItem();
        subItem.setName("Music Sub");
        subItem.setBasePrice(7900);
        subItem.setBillingCycleDays(30);
        subItem.setTrialDays(7);
        em.persist(subItem);

        em.flush();
        em.clear();

        System.out.println("===== 다형성 조회 SQL =====");
        // JOINED 전략: ITEM LEFT JOIN NORMAL_ITEM LEFT JOIN SUBSCRIPTION_ITEM
        List<Item> items = em.createQuery("select i from Ch7ItemKt i order by i.id", Item.class)
                .getResultList();
        /*
            Hibernate:
    select
        i
    from
        Ch7ItemKt i
    order by
        i.id

         ;
         select
          i1_0.item_id,
                i1_0.dtype,
                i1_0.base_price,
                i1_0.created_at,
                i1_0.name,
                i1_0.updated_at,
                i1_1.sku,
                i1_1.stock_quantity,
                i1_2.billing_cycle_days,
                i1_2.trial_days
        from
            item_ch7_kt i1_0
        left join
            normal_item_ch7_kt i1_1
        on i1_0.item_id=i1_1.item_id
        left join
            subscription_item_ch7_kt i1_2
        on i1_0.item_id=i1_2.item_id
        order by
        i1_0.item_id

                */

        assertEquals(2, items.size());
        assertTrue(items.get(0) instanceof NormalItem);
        assertTrue(items.get(1) instanceof SubscriptionItem);
    }

    // ========== 3-4. 검증 3: 주문 조회 시 OrderItem / Item 조회 방식 확인 ==========

    @Test
    @DisplayName("3-4: Order 단건 조회 → OrderItem 지연 로딩 확인")
    void 주문_조회_지연로딩_확인() {
        // 데이터 준비
        NormalItem normalItem = new NormalItem();
        normalItem.setName("USB Cable");
        normalItem.setBasePrice(9900);
        normalItem.setSku("USB-001");
        normalItem.setStockQuantity(100);
        em.persist(normalItem);

        Order order = new Order();
        order.setOrderNo("ORD-20260204-0001");
        em.persist(order);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setItem(normalItem);
        orderItem.setItemNameSnapshot("USB Cable");
        orderItem.setUnitPriceSnapshot(9900);
        orderItem.setQuantity(2);
        orderItem.getOptions().add(new Option("color", "black"));
        order.getOrderItems().add(orderItem);

        em.flush();
        em.clear();

        // 1단계: Order 단건 조회
        // → Order만 조회. OrderItem은 @OneToMany(LAZY)라서 아직 안 가져옴
        System.out.println("===== 1단계: Order 조회 =====");
        Order found = em.find(Order.class, order.getId());
        /* Hibernate:
    select
        o1_0.order_id,
        o1_0.created_at,
        o1_0.order_no,
        o1_0.updated_at
    from
        orders_ch7_kt o1_0
    where
        o1_0.order_id=?
        */

        // 2단계: OrderItem 접근 (지연 로딩)
        // → getOrderItems().size() 호출 시점에 프록시가 초기화되면서 SQL 실행
        // → order_id로 해당 주문의 OrderItem만 조회
        System.out.println("===== 2단계: OrderItem 접근 =====");
        int size = found.getOrderItems().size();
        /* Hibernate:
    select
        oi1_0.order_id,
        oi1_0.order_item_id,
        oi1_0.created_at,
        oi1_0.item_id,
        oi1_0.item_name_snapshot,
        oi1_0.quantity,
        oi1_0.unit_price_snapshot,
        oi1_0.updated_at
    from
        order_item_ch7_kt oi1_0
    where
        oi1_0.order_id=?
        */
        assertEquals(1, size);

        // 3단계: Item 접근 (지연 로딩)
        // → getItem()이 @ManyToOne(LAZY)라서 실제 .getName() 호출 시점에 SQL 실행
        // → JOINED 전략이므로 부모(item_ch7_kt) + 자식(normal_item, subscription_item) 모두 LEFT JOIN
        // → LEFT JOIN인 이유: item_id만으로는 어떤 자식 타입인지 모르기 때문에 모든 자식 테이블을 JOIN해서 확인
        System.out.println("===== 3단계: Item 접근 =====");
        String itemName = found.getOrderItems().get(0).getItem().getName();
        /* Hibernate:
    select
        i1_0.item_id,
        i1_0.dtype,
        i1_0.base_price,
        i1_0.created_at,
        i1_0.name,
        i1_0.updated_at,
        i1_1.sku,
        i1_1.stock_quantity,
        i1_2.billing_cycle_days,
        i1_2.trial_days
    from
        item_ch7_kt i1_0
    left join
        normal_item_ch7_kt i1_1
            on i1_0.item_id=i1_1.item_id
    left join
        subscription_item_ch7_kt i1_2
            on i1_0.item_id=i1_2.item_id
    where
        i1_0.item_id=?
        */
        assertEquals("USB Cable", itemName);
    }
}
