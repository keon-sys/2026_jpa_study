package homework.chapter_4;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 4장 숙제 2번: UNIQUE 제약조건 테스트 (JUnit)
 *
 * 주의: IDENTITY 전략에서는 persist() 호출 시 즉시 INSERT가 실행됨!
 * 따라서 UNIQUE 위반 예외는 persist() 또는 flush() 시점에 발생할 수 있음
 */
class UniqueConstraintTestJUnit {

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

    private String uniqueId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    @DisplayName("같은 loginId로 두 번 저장 시 PersistenceException 발생")
    void testDuplicateLoginIdThrowsException() {
        tx.begin();

        String duplicateLoginId = "dup_login_" + uniqueId();

        // given: 첫 번째 User 저장
        User user1 = new User();
        user1.setLoginId(duplicateLoginId);
        user1.setEmail("test1_" + uniqueId() + "@example.com");
        user1.setName("홍길동" + uniqueId());
        user1.setAge(25);
        em.persist(user1);
        em.flush();

        // when & then: 같은 loginId로 두 번째 User 저장 시도 -> 예외 발생
        // IDENTITY 전략에서는 persist 시점에 INSERT 실행되므로 persist에서 예외 발생
        User user2 = new User();
        user2.setLoginId(duplicateLoginId);  // 중복!
        user2.setEmail("test2_" + uniqueId() + "@example.com");
        user2.setName("김철수" + uniqueId());
        user2.setAge(30);

        PersistenceException exception = assertThrows(PersistenceException.class, () -> {
            em.persist(user2);
            em.flush();  // IDENTITY가 아닌 경우 여기서 발생
        }, "같은 loginId로 저장 시 PersistenceException이 발생해야 함");

        assertNotNull(exception.getCause(), "원인 예외가 있어야 함");
    }

    @Test
    @DisplayName("같은 email로 두 번 저장 시 PersistenceException 발생")
    void testDuplicateEmailThrowsException() {
        tx.begin();

        String duplicateEmail = "same_" + uniqueId() + "@example.com";

        // given
        User user1 = new User();
        user1.setLoginId("user1_" + uniqueId());
        user1.setEmail(duplicateEmail);
        user1.setName("테스트1_" + uniqueId());
        user1.setAge(20);
        em.persist(user1);
        em.flush();

        // when & then
        User user2 = new User();
        user2.setLoginId("user2_" + uniqueId());
        user2.setEmail(duplicateEmail);  // 중복!
        user2.setName("테스트2_" + uniqueId());
        user2.setAge(21);

        PersistenceException exception = assertThrows(PersistenceException.class, () -> {
            em.persist(user2);
            em.flush();
        }, "같은 email로 저장 시 PersistenceException이 발생해야 함");

        assertNotNull(exception.getCause(), "원인 예외가 있어야 함");
    }

    @Test
    @DisplayName("같은 name+age 복합키로 두 번 저장 시 PersistenceException 발생")
    void testDuplicateNameAgeCompositeThrowsException() {
        tx.begin();

        String duplicateName = "복합테스트_" + uniqueId();
        int duplicateAge = 99;

        // given
        User user1 = new User();
        user1.setLoginId("composite1_" + uniqueId());
        user1.setEmail("comp1_" + uniqueId() + "@example.com");
        user1.setName(duplicateName);
        user1.setAge(duplicateAge);
        em.persist(user1);
        em.flush();

        // when & then: 같은 name + age 조합
        User user2 = new User();
        user2.setLoginId("composite2_" + uniqueId());
        user2.setEmail("comp2_" + uniqueId() + "@example.com");
        user2.setName(duplicateName);   // 같은 name
        user2.setAge(duplicateAge);     // 같은 age

        PersistenceException exception = assertThrows(PersistenceException.class, () -> {
            em.persist(user2);
            em.flush();
        }, "같은 name+age 복합키로 저장 시 PersistenceException이 발생해야 함");

        assertNotNull(exception.getCause(), "원인 예외가 있어야 함");
    }

    @Test
    @DisplayName("다른 loginId로 저장 시 성공")
    void testDifferentLoginIdSuccess() {
        tx.begin();

        // given & when
        User user1 = new User();
        user1.setLoginId("success1_" + uniqueId());
        user1.setEmail("success1_" + uniqueId() + "@example.com");
        user1.setName("성공1_" + uniqueId());
        user1.setAge(10);
        em.persist(user1);

        User user2 = new User();
        user2.setLoginId("success2_" + uniqueId());  // 다른 loginId
        user2.setEmail("success2_" + uniqueId() + "@example.com");
        user2.setName("성공2_" + uniqueId());
        user2.setAge(11);
        em.persist(user2);

        // then: 예외 없이 flush 성공
        assertDoesNotThrow(() -> em.flush());

        tx.commit();
    }
}
