package com.barnwaldo.globaltable.serde;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import com.barnwaldo.globaltable.model.EnrichedOrder;

import java.util.Map;

public class EnrichedOrderSerde implements Serde<EnrichedOrder> {

	public void configure(Map<String, ?> map, boolean b) {

	}

	public void close() {

	}

	public Serializer<EnrichedOrder> serializer() {
		return new EnrichedOrderSerializer();
	}

	public Deserializer<EnrichedOrder> deserializer() {
		return new EnrichedOrderDeserializer();
	}
}
