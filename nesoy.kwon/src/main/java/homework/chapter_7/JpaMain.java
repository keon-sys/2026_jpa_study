package homework.chapter_7;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.util.Set;

public class JpaMain {
	public static void main(String[] args) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpabook");
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();

		try {
			tx.begin();

			// Items 생성
			NormalItem normalItem = new NormalItem("USB Cable", 9900, "USB-001", 100);
			SubscriptionItem subscriptionItem = new SubscriptionItem("Music Sub", 7900, 30, 7);
			em.persist(normalItem);
			em.persist(subscriptionItem);

			// Order 생성
			Order order = new Order();
			order.setOrderNo("ORD-20260204-0001");
			em.persist(order);

			// OrderItem 생성
			OrderItem비식별관계 orderItem1 = new OrderItem비식별관계(
				order, "USB Cable", 9900, 2,
				Set.of(new Option("color", "black"), new Option("size", "M"))
			);
			OrderItem비식별관계 orderItem2 = new OrderItem비식별관계(
				order, "Music Sub", 7900, 1,
				Set.of(new Option("trial", "true"))
			);
			em.persist(orderItem1);
			em.persist(orderItem2);

			em.flush();
			em.clear();

			// 3-3. 검증: 부모 타입(Item) 기준 다형성 조회
			System.out.println("### 3-3 다형성 조회");
			em.createQuery("select i from Item i order by i.id", Item.class)
				.getResultList();

			// 3-4. 검증: 주문 1건 조회 시 OrderItem 조회 방식 확인
			System.out.println("### 3-4-1 Order 단건 조회");
			Order foundOrder = em.find(Order.class, order.getId());

			System.out.println("### 3-4-2 getOrderItems() 접근");
			foundOrder.getOrderItems().size();

			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			e.printStackTrace();
		} finally {
			em.close();
			emf.close();
		}
	}
}
