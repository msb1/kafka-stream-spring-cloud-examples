package com.barnwaldo.globaltable.serde;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import com.barnwaldo.globaltable.model.Customer;

import java.util.Map;

public class CustomerSerde implements Serde<Customer> {

	public void configure(Map<String, ?> map, boolean b) {

	}

	public void close() {

	}

	public Serializer<Customer> serializer() {
		return new CustomerSerializer();
	}

	public Deserializer<Customer> deserializer() {
		return new CustomerDeserializer();
	}
}
