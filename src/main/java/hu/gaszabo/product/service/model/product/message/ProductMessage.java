package hu.gaszabo.product.service.model.product.message;

import hu.gaszabo.product.service.infrastructure.component.message.KafkaMessage;
import hu.gaszabo.product.service.infrastructure.util.UUIDGenerator;
import hu.gaszabo.product.service.model.product.Product;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ProductMessage implements KafkaMessage<Product> {

	private final Product product;
	private final String topicName;
	
	@Override
	public String topicName() {
		return topicName;
	}

	@Override
	public Product payload() {
		return product;
	}

	@Override
	public String messageKey() {
		return product.getId().toString();
	}

	@Override
	public String correlationId() {
		return UUIDGenerator.generate();
	}

}
