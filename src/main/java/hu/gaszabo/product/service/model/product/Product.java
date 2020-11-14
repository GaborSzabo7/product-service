package hu.gaszabo.product.service.model.product;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import hu.gaszabo.product.service.infrastructure.jpa.persistentobject.SequenceGeneratedPersistentEntity;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "PRODUCT")
public class Product extends SequenceGeneratedPersistentEntity {

	private static final long serialVersionUID = -5084855554067641943L;

	private static final int LENGTH_64 = 64;
	
	public static Product of(final String name, final String category, final long price) {
		return new Product(name, category, price);
	}

	@Column(name = "NAME", nullable = false, length = LENGTH_64)
	private String name;

	@Column(name = "CATEGORY", nullable = false, length = LENGTH_64)
	private String category;

	@Column(name = "PRICE", nullable = false)
	private long price;

}
