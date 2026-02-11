package homework.chapter_7;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "ORDERS")
public class Order extends BaseEntity {
	@Id
	@GeneratedValue
	private Long id;

	/**
	 * Unique Key
	 * e.g. ORD-20260204-0001
	 */
	@Column(nullable = false, unique = true)
	private String orderNo;

	@OneToMany(mappedBy = "order")
	private List<OrderItem비식별관계> orderItems;

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public List<OrderItem비식별관계> getOrderItems() {
		return orderItems;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}

@Entity
class OrderItem비식별관계 extends BaseEntity {
	@Id
	@GeneratedValue
	private Long id;

	/**
	 * Order FK를 참조한다.
	 */
	@ManyToOne
	@JoinColumn(name = "ORDER_ID")
	private Order order;

	/**
	 * Item.name 복사본
	 */
	@Column(nullable = false)
	private String itemNameSnapshot;
	/**
	 * Item.basePrice 복사본
	 */
	@Column(nullable = false)
	private long unitPriceSnapshot;

	@Column(nullable = false)
	private int quantity;

	@ElementCollection
	@CollectionTable(name = "ORDER_ITEM_OPTION",
			joinColumns = @JoinColumn(name = "ORDER_ITEM_ID"))
	private Set<Option> options = new HashSet<>();

	protected OrderItem비식별관계() {
	}

	public OrderItem비식별관계(Order order, String itemNameSnapshot, long unitPriceSnapshot, int quantity, Set<Option> options) {
		this.order = order;
		this.itemNameSnapshot = itemNameSnapshot;
		this.unitPriceSnapshot = unitPriceSnapshot;
		this.quantity = quantity;
		this.options = options;
	}
}

@Entity
class OrderItem식별관계 extends BaseEntity {
	@EmbeddedId
	private OrderItemId orderItemId;

	@ManyToOne
	@MapsId("orderId")
	private Order order;

	@ManyToOne
	@MapsId("itemId")
	private Item item;


	/**
	 * Item.name 복사본
	 */
	@Column(nullable = false)
	private String itemNameSnapshot;
	/**
	 * Item.basePrice 복사본
	 */
	@Column(nullable = false)
	private long unitPriceSnapshot;

	@Column(nullable = false)
	private int quantity;

	@ElementCollection
	@CollectionTable(name = "ORDER_ITEM_OPTION",
			joinColumns = @JoinColumn(name = "ORDER_ITEM_ID"))
	private Set<Option> options = new HashSet<>();
}

@Embeddable
class OrderItemId implements Serializable {

	@Column
	private Long orderId;

	@Column
	private Long itemId;

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		OrderItemId orderItemId1 = (OrderItemId) o;
		return Objects.equals(orderId, orderItemId1.orderId) && Objects.equals(itemId, orderItemId1.itemId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(orderId, itemId);
	}
}

/**
 * 값 타입
 * ID가 없기에 독립적으로 존재할 수 없음 -> OrderItem이 삭제 되면 같이 삭제됨.
 * 변경이 어려움. 전체 변경해야 함.
 */
@Embeddable
class Option {
	private String optionKey;
	private String optionValue;

	protected Option() {
	}

	public Option(String optionKey, String optionValue) {
		this.optionKey = optionKey;
		this.optionValue = optionValue;
	}
}

/**
 * 옵션 엔티티 분리
 * ID가 있기에 독립적으로 존재할 수 있음 -> OrderItem이 삭제 되면 같이 삭제되지 않음.
 * 별도의 테이블이라 수정이 쉽다. (수정이 빈번할때 사용하면 좋다.)
 */
@Entity
class OrderItemOption extends BaseEntity {
	@Id @GeneratedValue
	private Long id;

	@ManyToOne
	@JoinColumn(name = "ORDER_ITEM_ID")
	private OrderItem비식별관계 orderItem;

	private String optionKey;
	private String optionValue;
}