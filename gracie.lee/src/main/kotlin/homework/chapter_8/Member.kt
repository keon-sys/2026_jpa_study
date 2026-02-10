package homework.chapter_8

import jakarta.persistence.*

// 1) 엔티티 스켈레톤 - Member: id(PK), username(not null), team(@ManyToOne LAZY)
@Entity(name = "Ch8MemberKt")
@Table(name = "member_ch8_kt")
class Member(
    @Id
    @Column(name = "member_id")
    val id: Long? = null,

    @Column(nullable = false)
    var username: String = "",

    // 1) "team (@ManyToOne(fetch = LAZY))" 요구사항
    // → team에 접근하기 전까지 프록시 객체로 유지됨 (SQL 안 나감)
    // → 이것이 8장 전체 실험의 핵심: 프록시가 언제 초기화되고, 언제 위험해지는가?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    var team: Team? = null
) {
    // 1) "toString(), equals()/hashCode()에서 연관관계를 접근하지 않도록 주의"
    // → toString()에 team을 넣으면 프록시 초기화 → 의도치 않은 SQL이나 LazyInitializationException 발생
    override fun toString(): String {
        return "Member(id=$id, username='$username')"
    }
}
