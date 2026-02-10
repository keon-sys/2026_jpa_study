package homework.chapter_7

import jakarta.persistence.*
import java.time.LocalDateTime

@MappedSuperclass //  // 테이블 없음, 공통 필드만 자식에게 상속
abstract class BaseEntity(
    protected var createdAt: LocalDateTime? = null,
    protected var updatedAt: LocalDateTime? = null
) {
    //  INSERT 전
    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
    }

    //  UPDATE 전
    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
