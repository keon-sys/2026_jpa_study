package homework.chapter_8

import jakarta.persistence.*

// 1) 엔티티 스켈레톤 - Team: id(PK), name(not null)
@Entity(name = "Ch8TeamKt")
@Table(name = "team_ch8_kt")
class Team(
    @Id
    @Column(name = "team_id")
    val id: Long? = null,  // @GeneratedValue 없음 → 테스트에서 ID 직접 지정 (2번 요구사항)

    @Column(nullable = false)
    var name: String = ""
)
