package homework.chapter_7;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

@Entity
/**
 * create table Item (
 *         basePrice bigint not null,
 *         createdAt timestamp(6),
 *         id bigint not null,
 *         updatedAt timestamp(6),
 *         name varchar(255),
 *         primary key (id)
 *     )
 */
//@Inheritance(strategy = InheritanceType.JOINED)
/**
 * create table Item (
 *         billingCycleDays integer,
 *         stockQuantity integer,
 *         trialDays integer,
 *         basePrice bigint not null,
 *         createdAt timestamp(6),
 *         id bigint not null,
 *         updatedAt timestamp(6),
 *         DTYPE varchar(31) not null check ((DTYPE in ('NormalItem','SubscriptionItem'))),
 *         name varchar(255),
 *         sku varchar(255),
 *         primary key (id),
 *         check (DTYPE <> 'NormalItem' or (stockQuantity is not null)),
 *         check (DTYPE <> 'SubscriptionItem' or (billingCycleDays is not null and trialDays is not null))
 *     )
 */
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
/**
 * create table NormalItem (
 *         stockQuantity integer not null,
 *         basePrice bigint not null,
 *         createdAt timestamp(6),
 *         id bigint not null,
 *         updatedAt timestamp(6),
 *         name varchar(255),
 *         sku varchar(255),
 *         primary key (id)
 *     )
 */
//@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Item extends BaseEntity {
	@Id
	@GeneratedValue
	private long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private long basePrice;

	public Item(String name, long basePrice) {
		this.name = name;
		this.basePrice = basePrice;
	}

	public Item() {
	}
}

@Entity
class NormalItem extends Item {
	private String sku;
	private int stockQuantity;

	public NormalItem(String name, long basePrice, String sku, int stockQuantity) {
		super(name, basePrice);
		this.sku = sku;
		this.stockQuantity = stockQuantity;
	}

	public NormalItem() {
	}
}

@Entity
class SubscriptionItem extends Item {
	private int billingCycleDays;
	private int trialDays;

	public SubscriptionItem(String name, long basePrice, int billingCycleDays, int trialDays) {
		super(name, basePrice);
		this.billingCycleDays = billingCycleDays;
		this.trialDays = trialDays;
	}

	public SubscriptionItem() {
	}
}