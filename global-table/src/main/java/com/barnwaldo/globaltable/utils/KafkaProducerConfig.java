package com.barnwaldo.globaltable.utils;

import com.barnwaldo.globaltable.model.Customer;
import com.barnwaldo.globaltable.model.Order;
import com.barnwaldo.globaltable.model.Product;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, Customer> producerCustomerFactory() {
        return new DefaultKafkaProducerFactory<>(properties());
    }

    @Bean(name = "customerTemplate")
    public KafkaTemplate<String, Customer> customerTemplate() {
        return new KafkaTemplate<>(producerCustomerFactory());
    }

    @Bean
    public ProducerFactory<String, Order> producerOrderFactory() {
        return new DefaultKafkaProducerFactory<>(properties());
    }

    @Bean(name = "orderTemplate")
    public KafkaTemplate<String, Order> orderTemplate() {
        return new KafkaTemplate<>(producerOrderFactory());
    }

    @Bean
    public ProducerFactory<String, Product> producerProductFactory() {
        return new DefaultKafkaProducerFactory<>(properties());
    }

    @Bean(name = "productTemplate")
    public KafkaTemplate<String, Product> productTemplate() {
        return new KafkaTemplate<>(producerProductFactory());
    }

    private Map<String, Object> properties() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return props;
    }
}
