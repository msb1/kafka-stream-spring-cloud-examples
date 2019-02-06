package com.barnwaldo.toparticles.utils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.common.serialization.Serde;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.apache.avro.Schema;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Serialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.kstream.WindowedSerdes;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.binder.kafka.streams.annotations.KafkaStreamsProcessor;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import com.barnwaldo.toparticles.serde.PriorityQueueSerde;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;

/**
 * Example program derived from Confluent TopArticlesLambdaExample Kafka Streams
 * program is migrated to Spring Cloud with Kafka Streams Binders
 * 
 * In this example, the TopN articles from a stream of page views (aka clickstreams) are read
 * from a topic named "PageViews". 
 * 
 * The PageViews stream is filtered to only consider pages of type article, 
 * and then map the record key to effectively nullify the user, such that
 * page views are counted by (page, industry). 
 * 
 * The counts per (page, industry) are then grouped by industry and aggregated into a 
 * PriorityQueue with descending order. Finally mapValues are performed to fetch the top 100 articles per industry.
 * 
 * @author barnwaldo
 * @version 1.0
 * @since Feb 4, 2019
 */
@Component
public class PageViewStreamListener {

	@EnableBinding(KafkaStreamsProcessor.class)
	public class DataProcessorApplication {

		private final String schemaRegistryUrl = "http://192.168.5.4:8081";
		private Serde<GenericRecord> keyAvroSerde;
		private Serde<GenericRecord> valueAvroSerde;
		private Serde<Windowed<String>> windowedStringSerde;
		private Schema schema;

		public DataProcessorApplication() throws IOException {
			Map<String, String> serdeConfig = Collections
					.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
			keyAvroSerde = new GenericAvroSerde();
			valueAvroSerde = new GenericAvroSerde();
			keyAvroSerde.configure(serdeConfig, true);
			valueAvroSerde.configure(serdeConfig, false);
			windowedStringSerde = WindowedSerdes.timeWindowedSerdeFrom(String.class);
			schema = new Schema.Parser().parse(new File("src/main/avro/com/barnwaldo/pageviewstats.avsc"));
		}

		/**
		 *
		 * @param events
		 * @return
		 */
		@StreamListener("input")
		@SendTo("output")
		public KStream<Windowed<String>, String> process(KStream<String, GenericRecord> views) {

			final KStream<GenericRecord, GenericRecord> articleViews = views
					.peek((key, value) -> {
                        System.out.println(key + " -- " + value);
                    })
					// filter only article pages
					.filter((dummy, record) -> isArticle(record))
					// map <page id, industry> as key by making user the same for each record
					.map((dummy, article) -> {
						final GenericRecord clone = new GenericData.Record(article.getSchema());
						clone.put("user", "user");
						clone.put("page", article.get("page"));
						clone.put("industry", article.get("industry"));
						return new KeyValue<>(clone, clone);
					});

			KTable<Windowed<GenericRecord>, Long> viewCounts = articleViews
					// count the clicks per hour, using tumbling windows with a size of one hour
					.groupByKey(Serialized.with(keyAvroSerde, valueAvroSerde))
					.windowedBy(TimeWindows.of(TimeUnit.MINUTES.toMillis(60))).count();

			Comparator<GenericRecord> comparator = (o1, o2) -> (int) ((Long) o2.get("count") - (Long) o1.get("count"));

			KTable<Windowed<String>, PriorityQueue<GenericRecord>> allViewCounts = viewCounts.groupBy(
					// the selector
					(windowedArticle, count) -> {
						// project on the industry field for key
						Windowed<String> windowedIndustry = new Windowed<>(
								windowedArticle.key().get("industry").toString(), windowedArticle.window());
						// add the page into the value
						GenericRecord viewStats = new GenericData.Record(schema);
						viewStats.put("page", windowedArticle.key().get("page"));
						viewStats.put("user", "user");
						viewStats.put("industry", windowedArticle.key().get("industry"));
						viewStats.put("count", count);
						return new KeyValue<>(windowedIndustry, viewStats);
					}, Serialized.with(windowedStringSerde, valueAvroSerde)).aggregate(
							// the initializer
							() -> new PriorityQueue<>(comparator),
							// the "add" aggregator
							(windowedIndustry, record, queue) -> {
								queue.add(record);
								return queue;
							},
							// the "remove" aggregator
							(windowedIndustry, record, queue) -> {
								queue.remove(record);
								return queue;
							}, Materialized.with(windowedStringSerde,
									new PriorityQueueSerde<>(comparator, valueAvroSerde)));

			final int topN = 100;
			KTable<Windowed<String>, String> topViewCounts = allViewCounts.mapValues(queue -> {
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < topN; i++) {
					final GenericRecord record = queue.poll();
					if (record == null) {
						break;
					}
					sb.append(record.get("page").toString());
					sb.append("\n");
				}
				return sb.toString();
			});

			return topViewCounts.toStream();
		}

		private boolean isArticle(GenericRecord record) {
			Utf8 flags = (Utf8) record.get("flags");
			if (flags == null) {
				return false;
			}
			return flags.toString().contains("ARTICLE");
		}
	}

}
