package hu.gaszabo.product.service.infrastructure.kafka.component;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaMessageListener {

	@KafkaListener(topics = "${messaging.product.topic}")
	public void listenProductMessage(ConsumerRecord<String, String> record) {
		log.debug("Received record: {}", record.toString());
	}

}
