package com.ufg.hemoubiquitous_monitor;

import com.ufg.hemoubiquitous_monitor.debug.SubscriptionDebug;
import com.ufg.hemoubiquitous_monitor.factory.SubscriptionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HemoubiquitousMonitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(HemoubiquitousMonitorApplication.class, args);
		SubscriptionFactory.createHemogramSubscription();
		SubscriptionDebug.debugSubscriptions();
	}

}
