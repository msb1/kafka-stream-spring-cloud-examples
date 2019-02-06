package com.barnwaldo.mapfunctionlambda.utils;

import java.util.concurrent.TimeUnit;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.binder.kafka.streams.annotations.KafkaStreamsProcessor;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import com.barnwaldo.PageView;
import com.barnwaldo.PageViewWithRegion;
import com.barnwaldo.UserProfile;

/**
 * Example program derived from Confluent MapFunctionLambdaExample Kafka Streams
 * program is migrated to Spring Cloud with Kafka Streams Binders
 * 
 * Demonstrates how to perform a join between a KStream and a KTable, i.e. an
 * example of a stateful computation, using the generic Avro binding for serdes
 * in Kafka Streams.
 * 
 * @author barnwaldo
 * @version 1.0
 * @since Jan 26, 2019
 */
@Component
public class MapStreamListener {

	@EnableBinding(DataGen.class)
	public class DataProcessorApplication {

		/**
		 *
		 * @param textLines
		 * @return
		 */
		@StreamListener
		@SendTo("output")
		public KStream<Object, Long> process(@Input("input") KStream<String, PageView> views,
				@Input("users") KTable<String, UserProfile> userProfiles) {

			return views.map((dummy, record) -> new KeyValue<>(record.getUser(), record))

					.leftJoin(userProfiles.mapValues(record -> record.getRegion()), (view, region) -> {
						PageViewWithRegion viewRegion = new PageViewWithRegion();
						viewRegion.setPage(view.getPage());
						viewRegion.setUser(view.getUser());
						viewRegion.setRegion(region);
						return viewRegion;
					})

					.map((user, viewRegion) -> new KeyValue<>(viewRegion.get("region").toString(), viewRegion))
					// count views by region, using hopping windows of size 5 minutes that advance
					// every 1 minute
					.groupByKey()
					.windowedBy(TimeWindows.of(TimeUnit.MINUTES.toMillis(5)).advanceBy(TimeUnit.MINUTES.toMillis(1)))
					.count()
					// convert back to stream
					.toStream((windowedRegion, count) -> windowedRegion.toString() + " -- count: " + count);
		}
	}

	interface DataGen extends KafkaStreamsProcessor {

		@Input("users")
		KTable<?, ?> users();

	}

}
