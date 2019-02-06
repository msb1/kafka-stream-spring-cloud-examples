package com.barnwaldo.globaltable.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Order {

    private String customerId;
    private String productId;
    private String timeOrderPlacedAt;
    
    public Order(Order order) {
    	this.customerId = order.getCustomerId();
    	this.productId = order.getProductId();
    	this.timeOrderPlacedAt = order.getTimeOrderPlacedAt();
    }
}
