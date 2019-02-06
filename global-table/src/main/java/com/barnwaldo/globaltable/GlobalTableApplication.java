package com.barnwaldo.globaltable;

import com.barnwaldo.globaltable.model.Customer;
import com.barnwaldo.globaltable.model.EnrichedOrder;
import com.barnwaldo.globaltable.model.Order;
import com.barnwaldo.globaltable.model.Product;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * @author barnwaldo
 * @version 1.0
 * @since Jan 11, 2019
 * 
 * See TableStream listener for application details
 * formatted JSON is used without Avro in this application
 */
@SpringBootApplication
public class GlobalTableApplication {

	private int received = 0;
	private final String customerTopic;
	private final String orderTopic;
	private final String productTopic;
	private final int numRecords = 100;
	private final Random random = new Random();

	private final KafkaTemplate<String, Customer> customerTemplate;
	private final KafkaTemplate<String, Order> orderTemplate;
	private final KafkaTemplate<String, Product> productTemplate;

	@Autowired
	public GlobalTableApplication(KafkaTemplate<String, Customer> customerTemplate,
			KafkaTemplate<String, Order> orderTemplate, KafkaTemplate<String, Product> productTemplate) {
		this.customerTemplate = customerTemplate;
		this.orderTemplate = orderTemplate;
		this.productTemplate = productTemplate;
		customerTopic = "customers";
		orderTopic = "orders";
		productTopic = "products";
	}

	public static void main(String[] args) {
		SpringApplication.run(GlobalTableApplication.class, args);
	}

	// KafkaListener as Consumer for Kafka streams processed results
	@KafkaListener(topics = "enriched-orders", groupId = "barnwaldo")
	public void listen(ConsumerRecord<String, EnrichedOrder> record) {
		received += 1;
		if (record.value() != null) {
			System.out.println(String.format("EnrichedOrder %d received for product ** %s ** from %s", received,
					record.value().getProduct().getName(), record.value().getCustomer().getName()));
			System.out.println(record.value().toString() + "\n");
		} else {
			System.out.println(String.format("EnrichedOrder %d received: null", received));
		}
	}

	@Bean
	ApplicationRunner init() {
		// Producer to generate test records for this application
		return args -> {
			generateCustomers(numRecords);
			generateProducts(numRecords);
			generateOrders(numRecords, numRecords, numRecords);
		};
	}

	private void generateCustomers(int count) {
		String[] genders = { "male", "female", "unknown" };
		for (int i = 0; i < count; i++) {
			Customer customer = new Customer(randomString(10), genders[random.nextInt(genders.length)],
					randomString(20));
			customerTemplate.send(customerTopic, Integer.toString(i), customer);
		}
	}

	private void generateProducts(int count) {
		for (int i = 0; i < count; i++) {
			Product product = new Product(randomString(10), randomString(count), randomString(20));
			productTemplate.send(productTopic, Integer.toString(i), product);
		}
	}

	private void generateOrders(int count, int numCustomers, int numProducts) {
		for (int i = 0; i < count; i++) {
			String customerId = Integer.toString(random.nextInt(numCustomers));
			String productId = Integer.toString(random.nextInt(numProducts));
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			Order order = new Order(customerId, productId, dtf.format(now));
			orderTemplate.send(orderTopic, Integer.toString(i), order);
		}
	}

	// Copied from org.apache.kafka.test.TestUtils
	private String randomString(final int len) {
		final StringBuilder b = new StringBuilder();

		for (int i = 0; i < len; ++i) {
			b.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
					.charAt(random.nextInt("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".length())));
		}

		return b.toString();
	}

}
