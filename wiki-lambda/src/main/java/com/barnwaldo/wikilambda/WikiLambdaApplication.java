package com.barnwaldo.wikilambda;

import java.io.IOException;
import java.util.Random;
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

import com.barnwaldo.WikiFeed;

/**
 * @author barnwaldo
 * @version 1.0
 * @since Feb 6, 2019
 * 
 * See application comments in WikiStreamListener
 */
@SpringBootApplication
public class WikiLambdaApplication {

	private final String producerTopic;
	private final KafkaTemplate<String, WikiFeed> producerTemplate;
	private final Random random;

	private final String[] users = { "erica", "bob", "joe", "damian", "tania", "phil", "sam", "lauren", "joseph" };

	@Autowired
	public WikiLambdaApplication(KafkaTemplate<String, WikiFeed> producerTemplate) throws IOException {
		this.producerTopic = "wiki-feeds";
		this.producerTemplate = producerTemplate;
		this.random = new Random();
	}

	public static void main(String[] args) {
		SpringApplication.run(WikiLambdaApplication.class, args);
	}

	// KafkaListener as consumer of stream processed results
	@KafkaListener(topics = "wiki-stats", groupId = "barnwaldo")
	public void listen(ConsumerRecord<String, Long> record) {
		System.out.println(record.key() + " = " + record.value());
	}

	@Bean
	ApplicationRunner init() {
		// Produce (send) records to topic for processing with streams
		return args -> {

			IntStream.range(0, random.nextInt(100) + 50)
					.mapToObj(value -> new WikiFeed(users[random.nextInt(users.length)], true, "content"))
					.forEach(record -> {
						producerTemplate.send(new ProducerRecord<>(producerTopic, null, record));
						// System.out.println("Producer: " + record);
					});
		};
	}

}
