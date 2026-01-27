package homework.chapter_3;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 3장 숙제 2번: 1차 캐시와 동일성 보장 테스트 (JUnit)
 */
class CacheTestJUnit {

    private static EntityManagerFactory emf;
    private EntityManager em;
    private EntityTransaction tx;

    @BeforeAll
    static void setUpFactory() {
        emf = Persistence.createEntityManagerFactory("jpabook");
    }

    @AfterAll
    static void closeFactory() {
        if (emf != null) {
            emf.close();
        }
    }

    @BeforeEach
    void setUp() {
        em = emf.createEntityManager();
        tx = em.getTransaction();
        tx.begin();
    }

    @AfterEach
    void tearDown() {
        if (tx.isActive()) {
            tx.rollback();
        }
        if (em != null) {
            em.close();
        }
    }

    @Test
    @DisplayName("같은 영속성 컨텍스트 내에서 같은 id로 조회하면 동일한 객체 반환 (동일성 보장)")
    void testIdentityInSamePersistenceContext() {
        // given: 회원을 영속 상태로 만들기
        Member member = new Member();
        member.setId(1L);
        member.setName("홍길동");
        em.persist(member);

        // when: 같은 id로 두 번 조회
        Member findMember1 = em.find(Member.class, 1L);
        Member findMember2 = em.find(Member.class, 1L);

        // then: 동일한 객체여야 함 (1차 캐시)
        assertSame(findMember1, findMember2, "같은 영속성 컨텍스트 내에서는 동일한 객체여야 함");
        assertEquals("홍길동", findMember1.getName());
    }

    @Test
    @DisplayName("clear() 후 조회하면 새로운 객체 반환")
    void testNewObjectAfterClear() {
        // given: 회원을 영속 상태로 만들고 DB에 반영
        Member member = new Member();
        member.setId(2L);
        member.setName("김철수");
        em.persist(member);
        em.flush();

        Member findMemberBeforeClear = em.find(Member.class, 2L);

        // when: 영속성 컨텍스트 초기화 후 다시 조회
        em.clear();
        Member findMemberAfterClear = em.find(Member.class, 2L);

        // then: clear 전후 객체는 다른 객체여야 함
        assertNotSame(findMemberBeforeClear, findMemberAfterClear,
                "clear() 후에는 새로운 객체가 생성되어야 함");

        // 하지만 데이터는 같아야 함
        assertEquals(findMemberBeforeClear.getId(), findMemberAfterClear.getId());
        assertEquals(findMemberBeforeClear.getName(), findMemberAfterClear.getName());
    }

    @Test
    @DisplayName("1차 캐시에서 조회 시 SELECT 쿼리 없음")
    void testNoDatabaseQueryFromFirstLevelCache() {
        // given: 회원을 영속 상태로 만들기
        Member member = new Member();
        member.setId(3L);
        member.setName("테스트");
        em.persist(member);

        // when & then: persist 후 find는 1차 캐시에서 가져옴
        // (SQL 로그에서 SELECT 쿼리가 없음을 육안으로 확인)
        Member cached = em.find(Member.class, 3L);
        assertSame(member, cached, "persist한 객체와 find한 객체는 같아야 함");
    }
}