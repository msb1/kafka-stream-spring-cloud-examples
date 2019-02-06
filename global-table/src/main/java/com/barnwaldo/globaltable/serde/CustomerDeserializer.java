package com.barnwaldo.globaltable.serde;

import com.barnwaldo.globaltable.model.Customer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class CustomerDeserializer implements Deserializer<Customer> {

	private ObjectMapper mapper = new ObjectMapper();

	public void configure(Map<String, ?> map, boolean b) {

	}

	public Customer deserialize(String s, byte[] bytes) {

		try {

			return mapper.readValue(bytes, Customer.class);
		} catch (Exception e) {

			return null;
		}
	}

	public void close() {

	}
}
