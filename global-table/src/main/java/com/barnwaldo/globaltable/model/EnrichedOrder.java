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
public class EnrichedOrder {
    
    private Product product;
    private Customer customer;
    private Order order;

}
