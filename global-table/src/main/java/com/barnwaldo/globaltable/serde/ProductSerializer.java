package com.barnwaldo.globaltable.serde;

import com.barnwaldo.globaltable.model.Product;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class ProductSerializer implements Serializer<Product> {
	private ObjectMapper mapper = new ObjectMapper();

	public void configure(Map<String, ?> map, boolean b) {

	}

	public byte[] serialize(String s, Product Product) {
		try {
			return mapper.writeValueAsBytes(Product);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {

	}
}
