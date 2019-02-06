package com.barnwaldo.globaltable.serde;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import com.barnwaldo.globaltable.model.Order;

import java.util.Map;

public class OrderSerde implements Serde<Order> {

	public void configure(Map<String, ?> map, boolean b) {

	}

	public void close() {

	}

	public Serializer<Order> serializer() {
		return new OrderSerializer();
	}

	public Deserializer<Order> deserializer() {
		return new OrderDeserializer();
	}
}
