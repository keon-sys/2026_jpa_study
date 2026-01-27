package homework.chapter_4;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

/**
 * 4장 숙제 2번: UNIQUE 제약조건 테스트
 * 같은 loginId로 두 번 저장 시도하여 예외 확인
 */
public class UniqueConstraintTest {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpabook");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // 첫 번째 User 저장
            User user1 = new User();
            user1.setLoginId("testuser");
            user1.setEmail("test@example.com");
            user1.setName("홍길동");
            user1.setAge(25);
            em.persist(user1);
            System.out.println("=== 첫 번째 User 저장 완료 ===");

            // flush하여 DB에 반영
            em.flush();
            System.out.println("=== flush 완료 (INSERT SQL 실행) ===");

            // 같은 loginId로 두 번째 User 저장 시도
            User user2 = new User();
            user2.setLoginId("testuser");  // 중복된 loginId!
            user2.setEmail("test2@example.com");
            user2.setName("김철수");
            user2.setAge(30);
            em.persist(user2);
            System.out.println("=== 두 번째 User 저장 시도 ===");

            // flush 시점에 UNIQUE 제약조건 위반 예외 발생
            em.flush();

            tx.commit();

        } catch (Exception e) {
            System.out.println("\n=== 예외 발생! ===");
            System.out.println("예외 타입: " + e.getClass().getSimpleName());
            System.out.println("원인: 같은 loginId('testuser')로 두 번 저장 시도 -> UNIQUE 제약조건 위반");

            // 원인 예외 출력
            Throwable cause = e.getCause();
            while (cause != null) {
                System.out.println("Caused by: " + cause.getClass().getSimpleName() + " - " + cause.getMessage());
                cause = cause.getCause();
            }

            if (tx.isActive()) {
                tx.rollback();
                System.out.println("=== 트랜잭션 롤백 완료 ===");
            }
        } finally {
            em.close();
            emf.close();
        }
    }
}