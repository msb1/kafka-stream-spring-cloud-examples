package com.barnwaldo.sessionwindows;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.apache.avro.Schema;
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
 * @since Feb 2, 2019
 * 
 * See SessionStreamListener for application details
 */
@SpringBootApplication
public class SessionWindowsApplication {

	private int received = 0;
	private final Long INACTIVITY_GAP = TimeUnit.MINUTES.toMillis(30);
	private final long start = System.currentTimeMillis();
	private final String sessionTopic;

	private final KafkaTemplate<String, GenericRecord> producerTemplate;

	@Autowired
	public SessionWindowsApplication(KafkaTemplate<String, GenericRecord> producerTemplate) {
		this.producerTemplate = producerTemplate;
		sessionTopic = "play-events";
	}

	public static void main(String[] args) {
		SpringApplication.run(SessionWindowsApplication.class, args);
	}

	// KafkaListener as Consumer for data processed by Kafka Streams
	@KafkaListener(topics = "play-events-per-session", groupId = "barnwaldo")
	public void listen(ConsumerRecord<String, Long> record) {
		received += 1;
		System.out.println("message received: " + received + " --- " + record.key() + " = " + record.value());
	}

	@Bean
	ApplicationRunner init() {
		// Producer for test data to be processed by Kafka Streams
		return args -> {
			GenericRecordBuilder playEventBuilder = new GenericRecordBuilder(loadSchema("playevent.avsc"));
			
			// create three sessions with different times
			playEventBuilder.set("song_id", 1L);
			playEventBuilder.set("duration", 10L);
			producerTemplate.send(sessionTopic, null, start, "jo", playEventBuilder.build());
			
			playEventBuilder.set("song_id", 2L);
			playEventBuilder.set("duration", 10L);
			producerTemplate.send(sessionTopic, null, start + INACTIVITY_GAP / 10, "bill", playEventBuilder.build());
			
			playEventBuilder.set("song_id", 2L);
			playEventBuilder.set("duration", 10L);
			producerTemplate.send(sessionTopic, null, start + INACTIVITY_GAP / 5, "sarah", playEventBuilder.build());
			
			// out-of-order event for jo that is outside inactivity gap so will create a new session
			playEventBuilder.set("song_id", 1L);
			playEventBuilder.set("duration", 10L);
			producerTemplate.send(sessionTopic, null, start + INACTIVITY_GAP + 1, "jo", playEventBuilder.build());

		    // extend current session for bill
			playEventBuilder.set("song_id", 2L);
			playEventBuilder.set("duration", 10L);
			producerTemplate.send(sessionTopic, null, start + INACTIVITY_GAP, "bill", playEventBuilder.build());
			
		    // new session for sarah
			playEventBuilder.set("song_id", 2L);
			playEventBuilder.set("duration", 10L);
			producerTemplate.send(sessionTopic, null, start + 2 * INACTIVITY_GAP, "sarah", playEventBuilder.build());
			
		    // send earlier event for jo that will merge the 2 previous sessions
			playEventBuilder.set("song_id", 1L);
			playEventBuilder.set("duration", 10L);
			producerTemplate.send(sessionTopic, null, start + INACTIVITY_GAP / 2, "jo", playEventBuilder.build());

		    // new session for bill
			playEventBuilder.set("song_id", 2L);
			playEventBuilder.set("duration", 10L);
			producerTemplate.send(sessionTopic, null, start + 3 * INACTIVITY_GAP, "bill", playEventBuilder.build());
			
		    // extend session session for sarah
		    // new session for sarah
			playEventBuilder.set("song_id", 2L);
			playEventBuilder.set("duration", 10L);
			producerTemplate.send(sessionTopic, null, start + 2 * INACTIVITY_GAP + INACTIVITY_GAP / 5, "sarah", playEventBuilder.build());
			
		};
	}

	private static Schema loadSchema(String name) throws IOException {
		File initialFile = new File("src/main/avro/com/barnwaldo/" + name);
		InputStream targetStream = new FileInputStream(initialFile);
		return new Schema.Parser().parse(targetStream);
	}

}
