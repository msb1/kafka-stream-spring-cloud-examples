package com.barnwaldo.sumlambda.utils;

import org.apache.kafka.streams.kstream.KStream;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.binder.kafka.streams.annotations.KafkaStreamsProcessor;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

/**
 * Example program derived from Confluent SumLambdaExample Kafka Streams
 * program is migrated to Spring Cloud with Kafka Streams Binders
 * 
 * Demonstrates how to use `reduce` to sum numbers
 *
 * @author barnwaldo
 * @version 1.0
 * @since Feb 4, 2019
 */
@Component
public class SumLambdaStreamListener {

	@EnableBinding(KafkaStreamsProcessor.class)
	public class SumProcessorApplication {

		/**
		 *
		 * @param input
		 * @return
		 */
		@StreamListener("input")
		@SendTo("output")
		public KStream<Integer, Integer> process(KStream<Integer, Integer> input) {

			return input
					// only interested in odd numbers.
					.filter((k, v) -> v % 2 != 0)
					// compute the total sum across ALL numbers, so must re-key all records to the
					// same key. This re-keying is required because in Kafka Streams a data record
					// is always a key-value pair, and KStream aggregations such as `reduce` operate on a
					// per-key basis.
					// The actual new key (here: `1`) we pick here doesn't matter as long it is the
					// same across all records.
					.selectKey((k, v) -> 1)
					// no need to specify explicit serdes because the resulting key and value types
					// match our default serde settings
					.groupByKey()
					// Add the numbers to compute the sum.
					.reduce((v1, v2) -> v1 + v2)
					// have KTable - must convert back to stream current state
					.toStream();
		}
	}
}
