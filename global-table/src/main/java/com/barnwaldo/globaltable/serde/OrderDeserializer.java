package com.barnwaldo.globaltable.serde;

import com.barnwaldo.globaltable.model.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class OrderDeserializer implements Deserializer<Order> {

	private ObjectMapper mapper = new ObjectMapper();

	public void configure(Map<String, ?> map, boolean b) {

	}

	public Order deserialize(String s, byte[] bytes) {

		try {

			return mapper.readValue(bytes, Order.class);
		} catch (Exception e) {

			return null;
		}
	}

	public void close() {

	}
}
