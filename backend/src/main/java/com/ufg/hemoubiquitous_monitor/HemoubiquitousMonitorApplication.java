package com.ufg.hemoubiquitous_monitor;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import com.ufg.hemoubiquitous_monitor.factory.SubscriptionFactory;
import org.hl7.fhir.r4.model.Subscription;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HemoubiquitousMonitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(HemoubiquitousMonitorApplication.class, args);
		SubscriptionFactory.createHemogramSubscription();
	}

}
