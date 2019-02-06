package com.barnwaldo.sessionwindows.utils;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Serialized;
import org.apache.kafka.streams.kstream.SessionWindows;
import org.apache.kafka.streams.state.SessionStore;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.binder.kafka.streams.annotations.KafkaStreamsProcessor;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import com.barnwaldo.PlayEvent;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

/**
 * Example program derived from Confluent SessionWindowsExample Kafka Streams
 * program is migrated to Spring Cloud with Kafka Streams Binders
 * 
 * Demonstrates counting user activity (play-events) into Session Windows.
 * 
 * Define a session as events received by a user that all fall within a
 * specified gap of inactivity. In this case, 30 minutes. The sessions are
 * constantly aggregated into the StateStore "play-events-per-session", they are
 * also output to a topic with the same name.
 * 
 * @author barnwaldo
 * @version 1.0
 * @since Feb 2, 2019
 */
@Component
public class SessionStreamListener {

	@EnableBinding(KafkaStreamsProcessor.class)
	public class DataProcessorApplication {

		private final String PLAY_EVENTS_PER_SESSION = "play-events-per-session";
		private final Long INACTIVITY_GAP = TimeUnit.MINUTES.toMillis(30);
		private final String schemaRegistryUrl = "http://192.168.5.4:8081";
		private final SpecificAvroSerde<PlayEvent> playEventSerde;

		public DataProcessorApplication() {
			playEventSerde = new SpecificAvroSerde<>();
			Map<String, String> serdeConfig = Collections
					.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
			playEventSerde.configure(serdeConfig, false);
		}

		/**
		 *
		 * @param events
		 * @return
		 */
		@StreamListener("input")
		@SendTo("output")
		public KStream<String, Long> process(KStream<String, PlayEvent> events) {

			return events
					// group by key so we can count by session windows
					.groupByKey(Serialized.with(Serdes.String(), playEventSerde))
					// window by session
					.windowedBy(SessionWindows.with(INACTIVITY_GAP))
					// count play events per session
					.count(Materialized.<String, Long, SessionStore<Bytes, byte[]>>as(PLAY_EVENTS_PER_SESSION)
							.withKeySerde(Serdes.String()).withValueSerde(Serdes.Long()))
					// convert to a stream so we can map the key to a string
					.toStream()
					// map key to a readable string
					.map((key, value) -> new KeyValue<>(
							key.key() + "@" + key.window().start() + "->" + key.window().end(), value));
		}
	}

}
