package com.barnwaldo.globaltable.utils;

import com.barnwaldo.globaltable.model.Customer;
import com.barnwaldo.globaltable.model.CustomerOrder;
import com.barnwaldo.globaltable.model.EnrichedOrder;
import com.barnwaldo.globaltable.model.Order;
import com.barnwaldo.globaltable.model.Product;

import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.binder.kafka.streams.annotations.KafkaStreamsProcessor;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

/**
 * Example program derived from Confluent GlobalTablesExample and GlobalTablesExampleDriver
 * Kafka Streams program is migrated to Spring Cloud with Kafka Streams Binders
 * Kafka producers are used for Customers, Orders and Product
 * A Kafka Consumer is used for EnrichedOrders
 * 
 * Kafka Streams binders with Spring Cloud implement the Global Tables
 * Json Serdes are written for each data class (Avro is not used in this implementation)
 *
 * @author barnwaldo
 * @version 1.0
 * @since Jan 11, 2019
 */
@Component
public class TableStreamListener {

//    private final StreamsBuilder builder = new StreamsBuilder();

	@EnableBinding(DataGen.class)
	public class DataAnalyticsProcessorApplication {

		/**
		 * DevNotes: compilation fails unless method returns a KStream
		 *
		 * @param ordersStream
		 * @param customers
		 * @param products
		 * @return
		 */
		@StreamListener
		@SendTo("output")
		public KStream<Object, EnrichedOrder> process(@Input("input") KStream<Object, Order> ordersStream,
				@Input("customers") GlobalKTable<Object, Customer> customers,
				@Input("products") GlobalKTable<Object, Product> products) {

			// Join the orders stream to the customer global table. As this is global table
			// we can use a non-key based join with out needing to repartition the input
			// stream
			KStream<Object, CustomerOrder> customerOrdersStream = ordersStream
					// .peek((key, value) -> System.out.println("ordersStream -- key: " + key + " --
					// value: " + value))
					.join(customers, (key, value) -> value.getCustomerId(),
							(order, customer) -> new CustomerOrder(customer, order));

			// Join the enriched customer order stream with the product global table. As
			// this is global table
			// we can use a non-key based join without needing to repartition the input
			// stream
			KStream<Object, EnrichedOrder> enrichedOrdersStream = customerOrdersStream
					// .peek((key, value) -> System.out.println("customerOrdersStream2 -- key: " +
					// key + " -- value: " + value.toString()))
					.join(products, (key, value) -> value.getOrder().getProductId(),
							(customerOrder, product) -> new EnrichedOrder(product, customerOrder.getCustomer(),
									customerOrder.getOrder()));

			return enrichedOrdersStream;
		}

	}

	interface DataGen extends KafkaStreamsProcessor {

		@Input("customers")
		GlobalKTable<?, ?> customers();

		@Input("products")
		GlobalKTable<?, ?> products();

	}
}
