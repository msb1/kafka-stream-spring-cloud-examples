package com.barnwaldo.wikilambda.utils;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.binder.kafka.streams.annotations.KafkaStreamsProcessor;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import com.barnwaldo.WikiFeed;

/**
 * Example program derived from Confluent WikiFeedAvroLambdaExample Kafka Streams
 * program is migrated to Spring Cloud with Kafka Streams Binders
 * 
 * Computes, for every minute the number of new user feeds from the Wikipedia feed irc stream. 
 * 
 * Note: The specific Avro binding is used for serialization/deserialization, where the 
 * class is auto-generated from its Avro schema by the gradle avro plugin.
 * 
 * @author barnwaldo
 * @version 1.0
 * @since Feb 6, 2019
 */
@Component
public class WikiStreamListener {

	@EnableBinding(KafkaStreamsProcessor.class)
	public class DataProcessorApplication {

		/**
		 *
		 * @param feeds
		 * @return
		 */
		@StreamListener("input")
		@SendTo("output")
		public KStream<String, Long> process(KStream<String, WikiFeed> feeds) {

			// aggregate the new feed counts of by user
			return feeds
					// filter out old feeds
					.filter((dummy, value) -> value.getIsNew())
					// map the user id as key
					.map((key, value) -> new KeyValue<>(value.getUser(), value))
					.groupByKey().count().toStream()
					.peek((key, value) -> {
						// System.out.println(key + " -- " + value + " --- " + value.getIsNew());
                        System.out.println(key + " -- " + value);
                    });
		}

	}

}
