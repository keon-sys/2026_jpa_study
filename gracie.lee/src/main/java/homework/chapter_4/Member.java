package homework.chapter_4;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * 4장 숙제 1번: 다양한 매핑 어노테이션을 사용한 Member 엔티티
 *
 * 요구사항:
 * - 테이블명: members
 * - id: 기본키, 자동 생성 (IDENTITY 전략)
 * - username: VARCHAR(50), NOT NULL, 컬럼명 "user_name"
 * - age: Integer, NULL 허용
 * - email: VARCHAR(100), UNIQUE 제약조건
 * - role: enum 타입 (USER, ADMIN), 문자열로 저장
 * - createdAt: 날짜 타입 (DATE만 저장)
 * - description: CLOB 타입
 * - tempData: DB에 매핑하지 않는 임시 필드
 */
@Entity(name = "Chapter4Member")
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name", length = 50, nullable = false)
    private String username;

    private Integer age;

    @Column(length = 100, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Lob
    private String description;

    @Transient
    private String tempData;

    // 기본 생성자 (JPA 필수)
    public Member() {
    }

    // Getter & Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTempData() {
        return tempData;
    }

    public void setTempData(String tempData) {
        this.tempData = tempData;
    }
}