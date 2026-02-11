package homework.chapter_8;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 8장 과제: 프록시 객체와 영속성 컨텍스트 상태 변화 추적
 */
class Chapter8HomeworkTest {

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

        // ===== 2) 데이터 준비 =====
        // 요구사항: Team(id=10, name="team-A"), Member(id=1, username="member-1", team=team-A)
        // id 지정이 요구사항으로  @GeneratedValue 안 씀
        Team team = new Team(10L, "team-A");
        em.persist(team);

        Member member = new Member(1L, "member-1", team);
        em.persist(member);

        em.flush();
        em.clear();  // 데이터 준비 후 영속성 컨텍스트 초기화 (엔티티들은 준영속 상태가 됨)
    }

    @AfterEach
    void tearDown() {
        if (tx.isActive()) tx.rollback();
        if (em != null && em.isOpen()) em.close();
    }

    //  3-1) 기본 실험 코드 
    // 요구사항: teamProxy가 프록시인지 여부, SQL 실행 시점, 영속성 컨텍스트 상태를 설명하시오

    @Test
    @DisplayName("3-1: test_case_1 - 프록시 상태 변화 추적")
    void test_case_1() {
        //  4) SQL 로그: em.find(Member.class, 1L) 실행 시 SQL 
        Member m = em.find(Member.class, 1L);
        /*
            select m1_0.member_id, m1_0.team_id, m1_0.username
            from member_ch8_kt m1_0
            where m1_0.member_id=?
            -> Member만 SELECT. team은 LAZY라서 SQL 미실행
        */

        //  teamProxy가 프록시인지 여부
        Team teamProxy = m.getTeam();
        System.out.println("1) class = " + teamProxy.getClass());
        // 1) class = class homework.chapter_8.Team$HibernateProxy$d6Tm47Dx
        // 아직 초기화 안 된 프록시 객체

        //  0) @Transactional 범위 변경 실험: flush + clear 
        em.flush();
        em.clear();

        System.out.println("2) class = " + teamProxy.getClass());
        // 2) class = class homework.chapter_8.Team$HibernateProxy$d6Tm47Dx
        // 객체 참조는 계속 하고 있음. 여전히 프록시 객체

        //  SQL 로그: em.clear() 이후 동일 프록시로 접근 시 어떤 예외가 나오는지
        // -> LazyInitializationException 발생. 프록시 초기화에 영속성 컨텍스트가 필요한데 detached 상태
        assertThrows(LazyInitializationException.class, () -> {
            teamProxy.getName();
        });
    }

    //  3-2) 추가 케이스 A: clear 이후 "새로" getReference 비교 
    // 요구사항: getReference()의 의미를 비교하시오

    @Test
    @DisplayName("3-2: test_case_2 - clear 이후 새 getReference는 정상 동작")
    void test_case_2() {
        Member m = em.find(Member.class, 1L);
        Long teamId = m.getTeam().getId();  // getId()는 프록시가 이미 ID를 알고 있어서 초기화 안 됨

        em.flush();
        em.clear();  // 영속성 컨텍스트 비움

        // clear 후 em.getReference() -> 새 프록시 생성
        // 핵심: 이 프록시는 현재(clear 후의) 영속성 컨텍스트가 관리하는 "영속 상태" 프록시
        Team ref = em.getReference(Team.class, teamId);
        System.out.println("A) class = " + ref.getClass());
        // A) class = class homework.chapter_8.Team$HibernateProxy$Gsf3T0bJ

        //  4) SQL 로그: teamProxy.getName() 호출 시 SQL 
        // -> 영속 상태 프록시라서 초기화 성공! Team SELECT 실행
        System.out.println("B) name = " + ref.getName());
        // B) name = team-A
        assertEquals("team-A", ref.getName());

    }

    //  3-3) 추가 케이스 B: 트랜잭션 경계 밖에서 접근
    // 요구사항: LazyInitializationException이 발생하는 조건을 정확히 적는다
    //
    // 옵션 1은 Spring Boot의 @Transactional 필요 → 순수 JPA 프로젝트라 tx.commit() + em.close()로 동일 상황 재현
    // Spring @Transactional 메서드가 끝나는 것 = tx.commit() + em.close()

    @Test
    @DisplayName("3-3: 옵션 1 재현 - 서비스(@Transactional) 끝난 후 프록시 접근")
    void lazyInitializationException_after_tx_commit() {
        // ── 서비스 계층 역할 (@Transactional 안) ──
        // Spring이라면: @Transactional public Member getMember() { return em.find(...); }
        Member m = em.find(Member.class, 1L);  // team은 프록시 (초기화 안 됨)
        tx.commit();  // @Transactional 메서드 종료 = tx.commit()
        em.close();   // 영속성 컨텍스트 종료 (Spring이 자동으로 해주는 부분)

        // ── 호출자 역할 (tx 밖, 컨트롤러 등) ──
        // Spring이라면: Member member = memberService.getMember(1L);
        //              member.getTeam().getName();  // tx 밖!
        assertThrows(LazyInitializationException.class, () -> {
            m.getTeam().getName();  // 영속성 컨텍스트가 닫혀서 프록시 초기화 불가
        });
    }

    //  3-3) 옵션 2: clear로 detached 상태 재현 (3-1과의 차이: 발생 조건 3가지 정리)

    @Test
    @DisplayName("3-3: 옵션 2 - clear 후 detached 프록시 초기화 시도")
    void lazyInitializationException_after_clear() {
        Member m = em.find(Member.class, 1L);
        Team teamProxy = m.getTeam();   // LAZY -> 프록시 (초기화 안 됨)

        em.flush();
        em.clear();   // m, teamProxy 모두 detached 상태로 전환

        // LazyInitializationException 발생 조건 3가지가 모두 충족:
        // 1. 프록시가 초기화되지 않은 상태 (getName() 한 번도 안 호출)
        // 2. 영속성 컨텍스트에서 분리(detached)된 상태 (em.clear() 때문)
        // 3. 프록시 초기화를 트리거하는 메서드 호출 (getName())
        assertThrows(LazyInitializationException.class, () -> {
            teamProxy.getName();
        });
    }

    //  3-4) 위험한 줄 식별 
    // 요구사항: 아래 코드에서 위험한 줄은?
    // Member m = em.find(Member.class, 1L);  ← OK
    // Team t = m.getTeam();                   ← OK (프록시 반환)
    // em.clear();                             ← OK (영속성 컨텍스트 비움)
    // t.getName();                            ← 💥 위험! detached 프록시 초기화 시도

    @Test
    @DisplayName("3-4: 위험한 줄 식별 - 4번째 줄 t.getName()")
    void test_dangerous_line() {
        Member m = em.find(Member.class, 1L);   // 1줄: Member SELECT
        Team t = m.getTeam();                    // 2줄: 프록시 반환 (SQL 안 나감)
        em.clear();                              // 3줄: 영속성 컨텍스트 비움 -> t는 detached

        // 4줄: t.getName() ← 위험한 줄!
        // 이유: em.clear()로 t가 detached 되었는데, 초기화 안 된 프록시에 getName() 호출
        assertThrows(LazyInitializationException.class, () -> {
            t.getName();
        });
    }
}
