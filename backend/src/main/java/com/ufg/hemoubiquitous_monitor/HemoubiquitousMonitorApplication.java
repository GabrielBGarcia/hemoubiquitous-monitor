package com.ufg.hemoubiquitous_monitor;

import com.ufg.hemoubiquitous_monitor.subscription.SubscriptionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HemoubiquitousMonitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(HemoubiquitousMonitorApplication.class, args);
		SubscriptionFactory.createHemogramSubscription();
	}

}
