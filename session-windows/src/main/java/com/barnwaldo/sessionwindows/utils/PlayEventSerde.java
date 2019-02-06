package com.barnwaldo.sessionwindows.utils;

import java.util.Collections;
import java.util.Map;

import com.barnwaldo.PlayEvent;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer;

public class PlayEventSerde extends SpecificAvroSerializer<PlayEvent> {

	@Override
	public void configure(Map<String, ?> serializerConfig, boolean isSerializerForRecordKeys) {
		final Map<String, String> serdeConfig = Collections
				.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://192.168.5.4:8081");
		super.configure(serdeConfig, isSerializerForRecordKeys);
	}
}