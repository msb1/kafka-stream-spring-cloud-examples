package com.barnwaldo.sumlambda;

import java.util.stream.IntStream;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * 
 * @author barnwaldo
 * @version 1.0
 * @since Feb 4, 2019
 * 
 * See SumLambdaStreamListener for application details
 */
@SpringBootApplication
public class SumLambdaApplication {

	private int received = 0;
	private final String producerTopic;
	private final KafkaTemplate<Integer, Integer> producerTemplate;

	@Autowired
	public SumLambdaApplication(KafkaTemplate<Integer, Integer> producerTemplate) {
		this.producerTemplate = producerTemplate;
		producerTopic = "numbers";
	}

	public static void main(String[] args) {
		SpringApplication.run(SumLambdaApplication.class, args);
	}

	// KafkaListener as consumer for stream processed results
	@KafkaListener(topics = "sum-numbers", groupId = "barnwaldo")
	public void listen(ConsumerRecord<Integer, Integer> record) {
		received += 1;
		System.out.println(String.format("Message received: %d with key: %d -- current sum of numbers = %d", received,
				record.key(), record.value()));
	}

	@Bean
	ApplicationRunner init() {
		// Producer for data to send to Kafka stream for processing
		return args -> {
			IntStream.range(0, 100).mapToObj(val -> new ProducerRecord<>(producerTopic, val, val))
					.forEach(producerTemplate::send);
		};
	}
}
