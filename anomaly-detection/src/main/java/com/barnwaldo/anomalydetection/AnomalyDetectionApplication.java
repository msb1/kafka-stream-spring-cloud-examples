package com.barnwaldo.anomalydetection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * 
 * @author barnwaldo
 * @since Jan 11, 2019
 * version 1.0
 *  
 * DataStreamListener encapsulates application. See DataStream Listener for app details
 */
@SpringBootApplication
public class AnomalyDetectionApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnomalyDetectionApplication.class, args);
	}

}

