package homework.chapter_5.problem1;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 문제 1-3: 연관관계 주인의 중요성
 *
 * 목표: 주인이 아닌 쪽에서만 설정하면 DB에 반영 안 됨을 확인
 */
class Problem1_3Test {

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

    @Test
    @DisplayName("문제 1-3: team.getMembers().add(member)만 하면 FK가 null")
    void 주인이_아닌_쪽만_설정하면_FK가_null() {

        // 1. Team, Member 저장
        Team team = new Team("개발팀");
        em.persist(team);

        Member member = new Member("홍길동");
        em.persist(member);

        // 2. 주인이 아닌 곳에서 team.getMembers().add(member)만 호출 (1차 캐시, DB 반영안됌)
        team.getMembers().add(member);
        System.out.println("3. team.getMembers().add(member) 호출, member.setTeam(team)은 호출 안 함");

        // 3. flush/clear 후 Member를 다시 조회
        em.flush();
        em.clear();
        System.out.println("4. flush/clear 완료");

        // 4. member.getTeam()이 null인지 확인
        Member foundMember = em.find(Member.class, member.getId());
        // 출력된 SQL:
//        Hibernate:
//        select
//        m1_0.MEMBER_ID,
//                m1_0.name,
//                t1_0.TEAM_ID,
//                t1_0.name
//        from
//        CH5_P1_MEMBER m1_0
//        left join
//        CH5_P1_TEAM t1_0
//        on t1_0.TEAM_ID=m1_0.TEAM_ID
//        where
//        m1_0.MEMBER_ID=?

        // member.getTeam()이 null인지 확인
        assertNull(foundMember.getTeam());

        em.find(Team.class, team.getId());
        assertEquals(1, team.getMembers().size());

        Team reloadTeam = em.find(Team.class, team.getId());
        // 주인이 아닌 쪽에서만 설정했으므로 FK가 null → DB에서 조회하면 members가 비어있음
        assertEquals(0, reloadTeam.getMembers().size());
    }

    @Test
    @DisplayName("문제 1-3-fix: 연관관계 주인(member.setTeam)에서 설정하면 FK가 정상 반영")
    void 주인에서_설정하면_FK가_정상_반영() {

        // 1. Team, Member 저장
        Team team = new Team("개발팀");
        em.persist(team);

        Member member = new Member("홍길동");
        em.persist(member);

        // 2. 양쪽 모두 설정 (연관관계 주인에서도 설정!)
        team.getMembers().add(member);
        member.setTeam(team);  // ← 주인에서 설정! FK가 DB에 반영됨
        System.out.println("member.setTeam(team) 호출 → FK가 DB에 반영됨");

        // 3. flush/clear 후 다시 조회
        em.flush();
        em.clear();

        // 4. member.getTeam()이 null이 아닌지 확인
        Member foundMember = em.find(Member.class, member.getId());
        assertNotNull(foundMember.getTeam());
        assertEquals("개발팀", foundMember.getTeam().getName());

        // 5. Team을 다시 조회하면 members에 member가 있음 (FK가 DB에 있으니까!)
        Team reloadTeam = em.find(Team.class, team.getId());
        assertEquals(1, reloadTeam.getMembers().size());

        // 6. 같은 영속성 컨텍스트 내에서는 동일한 객체 → 이름 변경이 반영됨
        foundMember.setName("변경된이름");
        assertEquals("변경된이름", reloadTeam.getMembers().get(0).getName());  // ✅ 통과!
    }
}
