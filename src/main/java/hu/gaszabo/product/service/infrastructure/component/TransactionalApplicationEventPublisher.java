package hu.gaszabo.product.service.infrastructure.component;

import static java.util.Objects.requireNonNull;
import static org.springframework.util.Assert.isTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TransactionalApplicationEventPublisher implements Publisher<T> {

	private final ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	public TransactionalApplicationEventPublisher(final ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = requireNonNull(applicationEventPublisher, "applicationEventPublisher can't be null");
	}

	@Override
	public void publish(final T message) {
		log.debug("Attempt to publish message: {}", message);
		isTrue(TransactionSynchronizationManager.isSynchronizationActive(), "Event can't publish outside transaction");

		Optional<ApplicationEventPublisherSynchronization> activeSynchronization = findActiveSynchronization();
		if (activeSynchronization.isPresent()) {
			log.debug("Active EventPublisherSynchronization can be found");
			ApplicationEventPublisherSynchronization<T> synchronization = activeSynchronization.get();
			synchronization.registerEvent(message);
		} else {
			log.debug("Register EventPublisherSynchronization");
			TransactionSynchronizationManager.registerSynchronization(new ApplicationEventPublisherSynchronization<T>(message));
		}
	}

	private Optional<ApplicationEventPublisherSynchronization> findActiveSynchronization() {
		return TransactionSynchronizationManager.getSynchronizations() //
				.stream() //
				.filter(s -> s instanceof ApplicationEventPublisherSynchronization) //
				.map(s -> ApplicationEventPublisherSynchronization.class.cast(s)) //
				.findAny();
	}

	private final class ApplicationEventPublisherSynchronization<T> extends TransactionSynchronizationAdapter {

		private final List<T> events = new ArrayList<>();

		public ApplicationEventPublisherSynchronization(final T message) {
			events.add(message);
		}

		public void registerEvent(final T message) {
			events.add(message);
		}

		@Override
		public void afterCommit() {
			log.debug("Publishing event: {}", events);
			events.forEach(e -> {
				applicationEventPublisher.publishEvent(e);
			});
		}

	}

}
