package com.barnwaldo.globaltable.serde;

import com.barnwaldo.globaltable.model.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class OrderSerializer implements Serializer<Order> {
	private ObjectMapper mapper = new ObjectMapper();

	public void configure(Map<String, ?> map, boolean b) {

	}

	public byte[] serialize(String s, Order Order) {
		try {
			return mapper.writeValueAsBytes(Order);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {

	}
}
