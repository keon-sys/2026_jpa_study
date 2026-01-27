package homework.chapter_3;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

/**
 * 3장 숙제 2번: 1차 캐시와 동일성 보장을 테스트하는 코드
 */
public class CacheTest {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpabook");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();

        // 1. 새로운 회원을 생성하여 영속 상태로 만들기
        Member member = new Member();
        member.setId(1L);
        member.setName("홍길동");
        em.persist(member);
        // 예상 SQL: INSERT INTO MEMBER (ID, NAME, AGE) VALUES (1, '홍길동', null)
        // 실제로는 커밋 시점에 INSERT 쿼리가 실행됨
        System.out.println("=== 회원 영속 상태로 만들기 완료 ===");

        // 2. 같은 id로 두 번 조회하여 동일성(==) 비교
        // 예상 SQL: 1차 캐시에서 조회하므로 SELECT 쿼리 없음
        Member findMember1 = em.find(Member.class, 1L);
        System.out.println("첫 번째 조회 완료 (1차 캐시 히트 - SQL 없음)");

        // 예상 SQL: 1차 캐시에서 조회하므로 SELECT 쿼리 없음
        Member findMember2 = em.find(Member.class, 1L);
        System.out.println("두 번째 조회 완료 (1차 캐시 히트 - SQL 없음)");

        // 동일성 비교: 1차 캐시 덕분에 같은 객체 반환
        boolean isSameBeforeClear = (findMember1 == findMember2);
        System.out.println("clear 전 동일성 비교 (findMember1 == findMember2): " + isSameBeforeClear);
        // 결과: true (같은 영속성 컨텍스트 내에서 같은 식별자로 조회하면 동일한 객체 반환)

        // 3. 영속성 컨텍스트를 초기화(clear) 후 다시 조회
        em.flush(); // 먼저 변경사항을 DB에 반영
        em.clear(); // 영속성 컨텍스트 초기화 (모든 엔티티가 준영속 상태로 전환)
        System.out.println("=== 영속성 컨텍스트 초기화 (clear) ===");

        // 예상 SQL: SELECT * FROM MEMBER WHERE ID = 1
        // clear 후에는 1차 캐시가 비워졌으므로 DB에서 새로 조회
        Member findMemberAfterClear = em.find(Member.class, 1L);
        System.out.println("clear 후 조회 완료 (DB에서 SELECT 쿼리 실행)");

        // 4. 초기화 전후의 엔티티가 같은 객체인지 비교하고 결과 출력
        boolean isSameAfterClear1 = (findMember1 == findMemberAfterClear);
        boolean isSameAfterClear2 = (findMember2 == findMemberAfterClear);

        System.out.println("clear 후 동일성 비교 (findMember1 == findMemberAfterClear): " + isSameAfterClear1);
        System.out.println("clear 후 동일성 비교 (findMember2 == findMemberAfterClear): " + isSameAfterClear2);
        // 결과: 둘 다 false
        // 이유: clear() 후 새로 조회한 엔티티는 새로운 객체로 생성됨
        // 기존 엔티티(findMember1, findMember2)는 준영속 상태가 되어 더 이상 영속성 컨텍스트가 관리하지 않음

        System.out.println("\n=== 결론 ===");
        System.out.println("1. 같은 영속성 컨텍스트 내에서 같은 식별자로 조회하면 항상 동일한 객체 반환 (동일성 보장)");
        System.out.println("2. clear() 후에는 1차 캐시가 비워져 DB에서 새로 조회하며, 새로운 객체가 생성됨");
        System.out.println("3. 따라서 clear() 전후의 엔티티는 서로 다른 객체임");

        tx.commit();
        em.close();
        emf.close();
    }
}