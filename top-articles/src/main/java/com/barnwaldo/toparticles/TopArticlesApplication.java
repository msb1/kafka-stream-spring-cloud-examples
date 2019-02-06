package com.barnwaldo.toparticles;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.stream.IntStream;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.streams.kstream.Windowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * @author barnwaldo
 * @version 1.0
 * @since Feb 4, 2019
 * 
 * See PageViewStreamListener for application details.
 */
@SpringBootApplication
public class TopArticlesApplication {

	private final String producerTopic;
	private final KafkaTemplate<String, GenericRecord> producerTemplate;
	private final GenericRecordBuilder pageViewBuilder;
	final Random random;

	private final String[] users = { "erica", "bob", "joe", "damian", "tania", "phil", "sam", "lauren", "joseph" };
	private final String[] industries = { "engineering", "telco", "finance", "health", "science" };
	private final String[] pages = { "index.html", "news.html", "contact.html", "about.html", "stuff.html" };

	@Autowired
	public TopArticlesApplication(KafkaTemplate<String, GenericRecord> producerTemplate) throws IOException {
		random = new Random();
		pageViewBuilder = new GenericRecordBuilder(loadSchema("pageview.avsc"));
		this.producerTemplate = producerTemplate;
		producerTopic = "page-views";
	}

	public static void main(String[] args) {
		SpringApplication.run(TopArticlesApplication.class, args);
	}

	// KafkaListener as consumer for results of stream processing
	@KafkaListener(topics = "top-news-per-industry", groupId = "barnwaldo")
	public void listen(ConsumerRecord<Windowed<String>, String> record) {
		System.out.println(record.key().key() + "@" + record.key().window().start() +  "=" + record.value());
	}
	
	@Bean
	ApplicationRunner init() {
		// Producer sends data to Kafka Streams for processing
		return args -> {

			for (final String user : users) {
				pageViewBuilder.set("industry", industries[random.nextInt(industries.length)]);
				pageViewBuilder.set("flags", "ARTICLE");
				// For each user generate some page views
				IntStream.range(0, random.nextInt(10)).mapToObj(value -> {
					pageViewBuilder.set("user", user);
					pageViewBuilder.set("page", pages[random.nextInt(pages.length)]);
					return pageViewBuilder.build();
				}).forEach(record -> producerTemplate.send(new ProducerRecord<>(producerTopic, null, record)));
			}
		};
	}

	private static Schema loadSchema(String name) throws IOException {
		File initialFile = new File("src/main/avro/com/barnwaldo/" + name);
		InputStream targetStream = new FileInputStream(initialFile);
		return new Schema.Parser().parse(targetStream);
	}

}
