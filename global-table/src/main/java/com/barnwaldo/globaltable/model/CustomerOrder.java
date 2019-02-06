package com.barnwaldo.globaltable.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author  barnwaldo 
 * @version 
 * @since   Jan 21, 2019
 */
@Getter @Setter
@ToString
public class CustomerOrder {
    private Customer customer;
    private Order order;
    
    public CustomerOrder(Customer customer, Order order) {
    	this.customer = customer;
    	this.order = order;
    }
}
