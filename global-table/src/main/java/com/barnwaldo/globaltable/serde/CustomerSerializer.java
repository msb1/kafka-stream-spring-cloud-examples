package com.barnwaldo.globaltable.serde;

import com.barnwaldo.globaltable.model.Customer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class CustomerSerializer implements Serializer<Customer> {
	private ObjectMapper mapper = new ObjectMapper();

	public void configure(Map<String, ?> map, boolean b) {

	}

	public byte[] serialize(String s, Customer customer) {
		try {
			return mapper.writeValueAsBytes(customer);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {

	}
}
