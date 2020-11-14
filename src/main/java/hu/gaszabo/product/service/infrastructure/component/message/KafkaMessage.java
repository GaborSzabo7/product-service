package hu.gaszabo.product.service.infrastructure.component.message;

public interface KafkaMessage<T> extends TopicAware, IHavePayload<T>, IHaveMessageKey, IHaveCorrelationId, IHaveTimestamp {

}
