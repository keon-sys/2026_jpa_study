package homework.chapter_7

import jakarta.persistence.*

@Entity(name = "Ch7NormalItemKt")
@Table(name = "normal_item_ch7_kt")
@DiscriminatorValue("NORMAL") // DTYPE에 들어갈 값
class NormalItem(
    @Column(nullable = false)
    var sku: String = "",

    @Column(nullable = false)
    var stockQuantity: Int = 0
) : Item()

// INSERT 시 2번 실행: item_ch7_kt에 공통 필드 + normal_item_ch7_kt에 고유 필드
// SELECT 시 JOIN 발생: item_ch7_kt + normal_item_ch7_kt