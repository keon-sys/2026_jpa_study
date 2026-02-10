package homework.chapter_7

import jakarta.persistence.*

// 2-4. @Embeddable+@ElementCollection(A) / 엔티티 분리(B) 중 A 선택
@Embeddable // 독립된 테이블이 아닌, 다른 엔티티에 포함되어 사용되는 클래스
data class Option(
    @Column(name = "option_key")
    val key: String = "", // 옵션 이름 예: 색상, 사이즈

    @Column(name = "option_value")
    val value: String = "" // 옵션 값 예: 빨강, XL
)
