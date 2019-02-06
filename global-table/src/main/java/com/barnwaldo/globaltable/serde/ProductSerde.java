package com.barnwaldo.globaltable.serde;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import com.barnwaldo.globaltable.model.Product;

import java.util.Map;

public class ProductSerde implements Serde<Product> {

	public void configure(Map<String, ?> map, boolean b) {

	}

	public void close() {

	}

	public Serializer<Product> serializer() {
		return new ProductSerializer();
	}

	public Deserializer<Product> deserializer() {
		return new ProductDeserializer();
	}
}
