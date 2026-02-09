package homework.chapter_7;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;

/**
 * 아래의 형태처럼 들어감.
 *     create table Item (
 *         basePrice bigint not null,
 *         createdAt timestamp(6),
 *         id bigint not null,
 *         updatedAt timestamp(6),
 *         name varchar(255) not null,
 *         primary key (id)
 *     )
 */
@MappedSuperclass
public abstract class BaseEntity {
	protected LocalDateTime createdAt;
	protected LocalDateTime updatedAt;

	/**
	 * 저장하기 전에 진행한다.
	 */
	@PrePersist
	protected void prePersist() {
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
	}

	/**
	 * 업데이트하기 전에 진행한다.
	 */
	@PreUpdate
	protected void preUpdate() {
		updatedAt = LocalDateTime.now();
	}
}
