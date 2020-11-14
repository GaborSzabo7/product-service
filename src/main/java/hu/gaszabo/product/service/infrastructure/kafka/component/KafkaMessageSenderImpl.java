package hu.gaszabo.product.service.infrastructure.kafka.component;

import static java.util.Objects.requireNonNull;
import static org.springframework.kafka.support.KafkaHeaders.CORRELATION_ID;
import static org.springframework.kafka.support.KafkaHeaders.MESSAGE_KEY;
import static org.springframework.kafka.support.KafkaHeaders.TIMESTAMP;
import static org.springframework.kafka.support.KafkaHeaders.TOPIC;

import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import hu.gaszabo.product.service.infrastructure.component.message.KafkaMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaMessageSenderImpl implements KafkaMessageSender {

	private final KafkaTemplate<String, String> kafkaTemplate;

	public KafkaMessageSenderImpl(final KafkaTemplate<String, String> kafkaTemplate) {
		this.kafkaTemplate = requireNonNull(kafkaTemplate, "kafkaTemplate must not be null");
	}

	@Override
	public <P, T extends KafkaMessage<P>> void send(T message) {
		// requireNonNull(message, "message can't be null");

		kafkaTemplate //
				.send(createMessage(message)) //
				.addCallback(result -> {
					log.debug("Message successfully sent: {}", result);
				}, e -> {
					throw new IllegalStateException(e);
				});
	}

	@Override
	public <P, T extends KafkaMessage<P>> void send(List<T> messages) {
		requireNonNull(messages, "messages can't be null");

		messages.stream().forEach(message -> {
			kafkaTemplate //
					.send(createMessage(message)) //
					.addCallback(result -> {
						log.debug("Message successfully sent: {}", result);
					}, e -> {
						throw new IllegalStateException(e);
					});
		});
	}

	private static <P, T extends KafkaMessage<P>> Message<P> createMessage(final T message) {
		return MessageBuilder //
				.withPayload(message.payload()) //
				.setHeader(TOPIC, message.topicName()) //
				.setHeader(MESSAGE_KEY, message.messageKey()) //
				.setHeader(CORRELATION_ID, message.correlationId()) //
				.setHeader(TIMESTAMP, message.timestamp().toEpochMilli()) //
				.build();
	}

}
