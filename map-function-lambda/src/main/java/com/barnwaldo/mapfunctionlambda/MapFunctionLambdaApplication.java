package com.barnwaldo.mapfunctionlambda;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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
 * @since Jan 26, 2019
 * 
 * See MapStreamListener for app details
 */
@SpringBootApplication
public class MapFunctionLambdaApplication {

	private int received;
	private final String userProfileTopic;
	private final String pageViewTopic;
	private final String[] users = {"erica", "bob", "joe", "damian", "tania", "phil", "sam", "lauren", "joseph"};
    private final String[] regions = {"europe", "usa", "asia", "africa"};

    private Random random = new Random();
	private final KafkaTemplate<String, GenericRecord> producerTemplate;

	@Autowired
	public MapFunctionLambdaApplication(KafkaTemplate<String, GenericRecord> producerTemplate) {
		this.producerTemplate = producerTemplate;
		received = 0;
		userProfileTopic = "user-profiles";
		pageViewTopic = "page-views";
	}

	public static void main(String[] args) {
		SpringApplication.run(MapFunctionLambdaApplication.class, args);
	}

	// KafkaListener as Consumer for stream processed data
	@KafkaListener(topics = "mapped", groupId = "barnwaldo")
	public void listen(ConsumerRecord<String, Long> record) {
		received += 1;
		System.out.println("message received: " + received + " -- " + record.key() + " = " + record.value());
	}

	@Bean
	ApplicationRunner init() {
		// Producer to generate data for Kafka streams app testing
		return args -> {
			GenericRecordBuilder pageViewBuilder = new GenericRecordBuilder(loadSchema("pageview.avsc"));
			GenericRecordBuilder userProfileBuilder = new GenericRecordBuilder(loadSchema("userprofile.avsc"));
			pageViewBuilder.set("industry", "eng");
			
			for (String user : users) {
				userProfileBuilder.set("experience", "some");
				userProfileBuilder.set("region", regions[random.nextInt(regions.length)]);
				producerTemplate.send(userProfileTopic, user, userProfileBuilder.build());

				// For each user generate some page views
				for (int i = 0; i < random.nextInt(10); i++) {
					pageViewBuilder.set("user", user);
					pageViewBuilder.set("page", "index.html");
					GenericData.Record record = pageViewBuilder.build();
					producerTemplate.send(pageViewTopic,null, record);
				}
			}

		};
	}

	private static Schema loadSchema(String name) throws IOException {
		File initialFile = new File("src/main/avro/com/barnwaldo/" + name);
		InputStream targetStream = new FileInputStream(initialFile);
		return new Schema.Parser().parse(targetStream);
	}

}
