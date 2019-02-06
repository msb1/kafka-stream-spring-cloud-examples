package com.barnwaldo.toparticles.serde;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.kstream.WindowedSerdes;

import java.util.Map;

public class WindowedStringSerde implements Serde<Windowed<String>> {

	public void configure(Map<String, ?> map, boolean b) {

	}

	public void close() {

	}

	public Serializer<Windowed<String>> serializer() {
		return WindowedSerdes.timeWindowedSerdeFrom(String.class).serializer();
	}

	public Deserializer<Windowed<String>> deserializer() {
		return WindowedSerdes.timeWindowedSerdeFrom(String.class).deserializer();
	}
}
