package com.ufg.hemoubiquitous_monitor;

import com.ufg.hemoubiquitous_monitor.config.AnalyticsProperties;
import com.ufg.hemoubiquitous_monitor.debug.SubscriptionDebug;
import com.ufg.hemoubiquitous_monitor.factory.SubscriptionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(AnalyticsProperties.class)
@EnableScheduling
public class HemoubiquitousMonitorApplication {
	public static void main(String[] args) {
		SpringApplication.run(HemoubiquitousMonitorApplication.class, args);
		SubscriptionFactory.createHemogramSubscription();
		SubscriptionDebug.debugSubscriptions();
	}

}
