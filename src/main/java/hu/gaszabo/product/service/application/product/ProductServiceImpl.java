package hu.gaszabo.product.service.application.product;

import static hu.gaszabo.product.service.model.product.specification.ProductSpecification.nameNotEq;
import static hu.gaszabo.product.service.model.product.specification.ProductSpecification.nameNotIn;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static org.springframework.util.Assert.isTrue;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import hu.gaszabo.product.service.infrastructure.component.SpreadSheetComponent;
import hu.gaszabo.product.service.model.product.Product;
import hu.gaszabo.product.service.model.product.message.ProductMessage;
import hu.gaszabo.product.service.model.product.repository.ProductRepository;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

	private static final String SHEET_NAME = "PRODUCTS";

	private final ProductRepository productRepository;
	private final SpreadSheetComponent spreadSheetComponent;

	@Autowired
	public ProductServiceImpl( //
			final ProductRepository productRepository, //
			final SpreadSheetComponent spreadSheetComponent) {

		this.productRepository = requireNonNull(productRepository, "productRepository can't be  null");
		this.spreadSheetComponent = requireNonNull(spreadSheetComponent, "spreadSheetComponent can't be  null");
	}

	@Override
	@Transactional(value = "transactionManager", propagation = REQUIRES_NEW)
	public void uploadFile(MultipartFile file) {
		requireNonNull(file, "file can't be null");

		List<ProductRow> products = spreadSheetComponent.processSheet(file, SHEET_NAME, ProductRow.class);
		List<Product> productEntities = (List<Product>) productRepository.saveAll(mapToProductEntity(products));
	}

	@Override
	@Transactional(value = "transactionManager", propagation = REQUIRES_NEW, readOnly = true)
	public ByteArrayInputStream synchronizeProducts(MultipartFile file) {
		requireNonNull(file, "file can't be null");

		List<ProductRow> products = spreadSheetComponent.processSheet(file, SHEET_NAME, ProductRow.class);

		// save those which not exist in the database
		products.stream() //
				.filter(p -> notExist(p.getName())) //
				.map(mapToProductEntity()) //
				.forEach(productRepository::save);

		// find all which not exist in the spreadsheet
		List<ProductRow> productRows = //
				productRepository.findAll(nameNotIn(mapToNames(products))) //
						.stream() //
						.map(mapToProductRow()) //
						.collect(toList());

		return (ByteArrayInputStream) spreadSheetComponent.appendSheet(file, SHEET_NAME, productRows);
	}

	// ------------------------------------------------------------------------------------------------------------------------
	// Private methods
	// ------------------------------------------------------------------------------------------------------------------------

	private Iterable<Product> mapToProductEntity(List<ProductRow> products) {
		return products.stream() //
				.map(p -> Product.of(p.getName(), p.getCategory(), p.getPrice())) //
				.collect(toList());
	}

	private static Function<Product, ProductMessage> mapToProductMessage(final String topicName) {
		isTrue(isNoneBlank(topicName), "topicName can't be blank");
		return p -> new ProductMessage(p, topicName);
	}

	private static List<String> mapToNames(List<ProductRow> products) {
		requireNonNull(products, "products can't be null");
		return products.stream().map(ProductRow::getName).collect(toList());
	}

	private boolean notExist(final String name) {
		return productRepository.exists(nameNotEq(name));
	}

	private Function<ProductRow, Product> mapToProductEntity() {
		return p -> Product.of(p.getName(), p.getCategory(), p.getPrice());
	}

	private Function<Product, ProductRow> mapToProductRow() {
		return p -> new ProductRow(p.getName(), p.getCategory(), p.getPrice());
	}

	@Getter
	@ToString
	static final class ProductRow {

		private final String name;
		private final String category;
		private final long price;

		@JsonCreator
		public ProductRow( //
				@JsonProperty(value = "name") String name, //
				@JsonProperty(value = "category") String category, //
				@JsonProperty(value = "price") long price) {
			this.name = name;
			this.category = category;
			this.price = price;
		}

	}

}
