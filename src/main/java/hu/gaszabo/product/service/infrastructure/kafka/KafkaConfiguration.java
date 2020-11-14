package hu.gaszabo.product.service.infrastructure.kafka;

import static org.springframework.util.ObjectUtils.nullSafeToString;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ErrorHandler;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

import hu.gaszabo.product.service.infrastructure.component.Publisher;
import hu.gaszabo.product.service.infrastructure.component.message.KafkaMessage;
import hu.gaszabo.product.service.infrastructure.kafka.component.KafkaMessageListener;
import hu.gaszabo.product.service.infrastructure.kafka.component.KafkaMessageSender;
import hu.gaszabo.product.service.infrastructure.kafka.component.KafkaMessageSenderImpl;
import hu.gaszabo.product.service.infrastructure.kafka.transaction.TransactionalKafkaMessagePublisher;

@Profile("kafka-dev")
@EnableKafka
@Configuration
public class KafkaConfiguration {

	@Autowired
	private ConsumerFactory<String, String> consumerfactory;

	@Autowired
	private ProducerFactory<String, String> producerFactory;

	@Autowired
	private ObjectMapper objectMapper;

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerfactory);
		factory.setMessageConverter(new StringJsonMessageConverter(objectMapper));
		factory.setErrorHandler(new LoggingErrorHandler());
		return factory;
	}

	@Bean
	public KafkaTemplate<String, String> kafkaTemplate() {
		KafkaTemplate<String, String> template = new KafkaTemplate<>(producerFactory);
		template.setMessageConverter(new StringJsonMessageConverter(objectMapper));
		return template;
	}

	static class LoggingErrorHandler implements ErrorHandler {

		private static final Logger log = LoggerFactory.getLogger(LoggingErrorHandler.class);

		@Override
		public void handle(final Exception thrownException, final ConsumerRecord<?, ?> record) {
			log.error("Error while processing: " + nullSafeToString(record), thrownException);
		}

	}

	@Bean
	public KafkaMessageSender kafkaMessageSender() {
		return new KafkaMessageSenderImpl(kafkaTemplate());
	}

	@Bean
	public KafkaMessageListener kafkaMessageListener() {
		return new KafkaMessageListener();
	}

	@Bean
	public <P> Publisher<KafkaMessage<P>> transactionalKafkaMessagePublisher() {
		return new TransactionalKafkaMessagePublisher<P>(kafkaMessageSender());
	}

}
