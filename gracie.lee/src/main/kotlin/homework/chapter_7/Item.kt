package homework.chapter_7

import jakarta.persistence.*

@Entity(name = "Ch7ItemKt")
@Table(name = "item_ch7_kt")
@Inheritance(strategy = InheritanceType.JOINED)  // 2-2. SINGLE_TABLE / JOINED / TABLE_PER_CLASS 중 JOINED 선택
@DiscriminatorColumn(name = "DTYPE") // 타입 구분 컬
abstract class Item(
    @Id
    @GeneratedValue
    @Column(name = "item_id")
    val id: Long? = null,

    @Column(nullable = false)
    var name: String = "",

    @Column(nullable = false)
    var basePrice: Long = 0
) : BaseEntity()
