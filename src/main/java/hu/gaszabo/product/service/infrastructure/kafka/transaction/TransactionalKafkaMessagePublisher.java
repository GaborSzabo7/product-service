package hu.gaszabo.product.service.infrastructure.kafka.transaction;

import static java.util.Objects.requireNonNull;
import static org.springframework.util.Assert.isTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import hu.gaszabo.product.service.infrastructure.component.Publisher;
import hu.gaszabo.product.service.infrastructure.component.message.KafkaMessage;
import hu.gaszabo.product.service.infrastructure.kafka.component.KafkaMessageSender;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionalKafkaMessagePublisher<P> implements Publisher<KafkaMessage<P>> {

	private final KafkaMessageSender kafkaMessageSender;

	@Autowired
	public TransactionalKafkaMessagePublisher(final KafkaMessageSender kafkaMessageSender) {
		this.kafkaMessageSender = requireNonNull(kafkaMessageSender, "kafkaMessageSender can't be null");
	}

	@Override
	public void publish(final KafkaMessage<P> message) {
		log.debug("Attemting to publish message: {}", message);
		isTrue(TransactionSynchronizationManager.isSynchronizationActive(), "Message can't be published");

		Optional<MessagePublisherSynchronization> activeSynchronization = findActiveSynchronization();
		if (activeSynchronization.isPresent()) {
			log.debug("Active EventPublisherSynchronization can be found");
			MessagePublisherSynchronization<KafkaMessage<P>> synchronization = activeSynchronization.get();
			synchronization.registerEvent(message);
		} else {
			log.debug("Register EventPublisherSynchronization");
			TransactionSynchronizationManager.registerSynchronization(new MessagePublisherSynchronization<KafkaMessage<P>>(message));
		}
	}

	private Optional<MessagePublisherSynchronization> findActiveSynchronization() {
		return TransactionSynchronizationManager.getSynchronizations() //
				.stream() //
				.filter(s -> s instanceof MessagePublisherSynchronization) //
				.map(s -> MessagePublisherSynchronization.class.cast(s)) //
				.findAny();
	}

	private final class MessagePublisherSynchronization<T extends KafkaMessage<P>> extends TransactionSynchronizationAdapter {

		private final List<T> messages = new ArrayList<>();

		public MessagePublisherSynchronization(final T message) {
			messages.add(message);
		}

		public void registerEvent(final T message) {
			messages.add(message);
		}

		@Override
		public void afterCommit() {
			log.debug("Publishing message: {}", messages);
			kafkaMessageSender.send(messages);
		}

	}

}
