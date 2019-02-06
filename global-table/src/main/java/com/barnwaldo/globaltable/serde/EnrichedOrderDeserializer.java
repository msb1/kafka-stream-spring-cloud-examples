package com.barnwaldo.globaltable.serde;

import com.barnwaldo.globaltable.model.EnrichedOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class EnrichedOrderDeserializer implements Deserializer<EnrichedOrder> {

	private ObjectMapper mapper = new ObjectMapper();

	public void configure(Map<String, ?> map, boolean b) {

	}

	public EnrichedOrder deserialize(String s, byte[] bytes) {

		try {

			return mapper.readValue(bytes, EnrichedOrder.class);
		} catch (Exception e) {

			return null;
		}
	}

	public void close() {

	}
}
