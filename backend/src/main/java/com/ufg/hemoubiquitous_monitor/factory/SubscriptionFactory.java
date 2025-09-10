package com.ufg.hemoubiquitous_monitor.factory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import com.ufg.hemoubiquitous_monitor.config.FhirConfig;
import org.hl7.fhir.r4.model.*;

public class SubscriptionFactory {
    public static Subscription createHemogramSubscription() {
        Subscription subscription = new Subscription();

        subscription.setStatus(Subscription.SubscriptionStatus.REQUESTED);
        subscription.setReason("Receber notificações de novos hemogramas");
        subscription.setCriteria("Observation?code=LOINC|718-7"); // 718-7 = Hemograma completo

        Subscription.SubscriptionChannelComponent channel = new Subscription.SubscriptionChannelComponent();
        channel.setType(Subscription.SubscriptionChannelType.RESTHOOK);
        channel.setEndpoint(FhirConfig.getFhirServerUrl()); // seu endpoint receptor
        channel.setPayload("application/fhir+json");

        subscription.setChannel(channel);
        createSubscription(subscription);
        return subscription;
    }

    private static void createSubscription(Subscription subscription) {
        FhirContext ctx = FhirContext.forR4();
        ctx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);

        ctx.getRestfulClientFactory().setConnectTimeout(30 * 1000);
        ctx.getRestfulClientFactory().setSocketTimeout(30 * 1000);
        IGenericClient client = ctx.newRestfulGenericClient("https://hapi.fhir.org/baseR4");

        MethodOutcome methodOutcome = client.create().resource(subscription).execute();

        System.out.println("Subscription registrada com sucesso!");
    }
}
