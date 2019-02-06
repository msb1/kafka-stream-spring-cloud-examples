package com.barnwaldo.globaltable.serde;

import com.barnwaldo.globaltable.model.EnrichedOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class EnrichedOrderSerializer implements Serializer<EnrichedOrder> {
	private ObjectMapper mapper = new ObjectMapper();

	public void configure(Map<String, ?> map, boolean b) {

	}

	public byte[] serialize(String s, EnrichedOrder EnrichedOrder) {
		try {
			return mapper.writeValueAsBytes(EnrichedOrder);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {

	}
}
