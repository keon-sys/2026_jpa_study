package homework.chapter_4;

import jakarta.persistence.*;

/**
 * 4장 숙제 2번: DDL 자동 생성 기능을 활용한 제약조건 설정
 *
 * 요구사항:
 * - loginId: UNIQUE 제약조건
 * - email: UNIQUE 제약조건
 * - name + age 복합 UNIQUE 제약조건 (테이블 레벨)
 * - age: 0 이상 150 이하 (CHECK 제약조건은 주석으로 표시)
 */
@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_name_age",
                        columnNames = {"name", "age"}
                )
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String loginId;

    @Column(unique = true)
    private String email;

    private String name;

    // age: 0 이상 150 이하 (CHECK 제약조건)
    // DDL: CHECK (age >= 0 AND age <= 150)
    // Hibernate에서는 @Check 어노테이션 사용 가능하나, 여기서는 주석으로 표시
    private Integer age;

    // 기본 생성자 (JPA 필수)
    public User() {
    }

    // Getter & Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}