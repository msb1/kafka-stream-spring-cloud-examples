package com.barnwaldo.toparticles.serde;

import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.kstream.WindowedSerdes;

public class WindowedStringDeserializer implements Deserializer<Windowed<String>> {

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
		
	}

	@Override
	public Windowed<String> deserialize(String topic, byte[] data) {		
		return WindowedSerdes.timeWindowedSerdeFrom(String.class).deserializer().deserialize(topic, data);
	}

	@Override
	public void close() {
		
	}

}
