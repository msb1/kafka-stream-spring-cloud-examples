package com.barnwaldo.globaltable.serde;

import com.barnwaldo.globaltable.model.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class ProductDeserializer implements Deserializer<Product> {

	private ObjectMapper mapper = new ObjectMapper();

	public void configure(Map<String, ?> map, boolean b) {

	}

	public Product deserialize(String s, byte[] bytes) {

		try {

			return mapper.readValue(bytes, Product.class);
		} catch (Exception e) {

			return null;
		}
	}

	public void close() {

	}
}
