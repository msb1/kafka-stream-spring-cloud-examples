package com.barnwaldo.wikilambda.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.barnwaldo.WikiFeed;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, WikiFeed> userProfileFactory() {
        return new DefaultKafkaProducerFactory<>(properties());
    }

    @Bean(name = "producerTemplate")
    public KafkaTemplate<String, WikiFeed> productTemplate() {
        return new KafkaTemplate<>(userProfileFactory());
    }

    private Map<String, Object> properties() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.5.4:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, io.confluent.kafka.serializers.KafkaAvroSerializer.class);
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,  "http://192.168.5.4:8081");
        return props;
    }
}

