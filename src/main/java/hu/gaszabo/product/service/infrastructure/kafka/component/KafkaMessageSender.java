package hu.gaszabo.product.service.infrastructure.kafka.component;

import java.util.List;

import hu.gaszabo.product.service.infrastructure.component.message.KafkaMessage;

public interface KafkaMessageSender {

	<P, T extends KafkaMessage<P>> void send(T message);

	<P, T extends KafkaMessage<P>> void send(List<T> messages);

}
