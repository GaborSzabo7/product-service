package hu.gaszabo.product.service.model.product.specification;

import java.util.List;

import com.querydsl.core.types.Predicate;

import hu.gaszabo.product.service.model.product.QProduct;

public class ProductSpecification {

	private static final QProduct PRODUCT = QProduct.product;

	private ProductSpecification() {
		throw new IllegalStateException("Class can't be instantiated.");
	}

	public static Predicate nameNotEq(final String name) {
		return PRODUCT.name.ne(name);
	}

	public static Predicate nameNotIn(final List<String> names) {
		return PRODUCT.name.notIn(names);
	}

}
