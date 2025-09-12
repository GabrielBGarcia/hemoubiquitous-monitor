package com.ufg.hemoubiquitous_monitor.subscription;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.Bundle;

public class SubscriptionDebug {

    public static void debugSubscriptions() {
        FhirContext ctx = FhirContext.forR4();
        IGenericClient client = ctx.newRestfulGenericClient("https://hapi.fhir.org/baseR4");

        try {
            System.out.println("🔎 Debug: Listando TODAS as subscriptions...");

            Bundle bundle = client.search()
                    .forResource(org.hl7.fhir.r4.model.Subscription.class)
                    .returnBundle(Bundle.class)
                    .execute();

            int count = 0;
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                if (entry.getResource() instanceof org.hl7.fhir.r4.model.Subscription) {
                    org.hl7.fhir.r4.model.Subscription sub =
                            (org.hl7.fhir.r4.model.Subscription) entry.getResource();

                    count++;
                    System.out.println("\n--- Subscription " + count + " ---");
                    System.out.println("ID: " + sub.getIdElement().getIdPart());
                    System.out.println("Status: " + sub.getStatus());
                    System.out.println("Criteria: " + sub.getCriteria());
                    System.out.println("Endpoint: " + sub.getChannel().getEndpoint());

                    if (sub.getError() != null) {
                        System.out.println("❌ ERRO: " + sub.getError());
                    }
                }
            }

            if (count == 0) {
                System.out.println("Nenhuma subscription encontrada!");
            }

        } catch (Exception e) {
            System.err.println("Erro no debug: " + e.getMessage());
        }
    }
}