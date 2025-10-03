package com.ufg.hemoubiquitous_monitor.factory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import com.ufg.hemoubiquitous_monitor.config.FhirConfig;
import org.hl7.fhir.r4.model.*;

public class SubscriptionFactory {
    private static String createdSubscriptionId;
    public static Subscription createHemogramSubscription() {
        Subscription subscription = new Subscription();

        subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        subscription.setReason("Receber notificações de novos hemogramas");
        subscription.setCriteria("Observation?code=718-7");

        Subscription.SubscriptionChannelComponent channel = new Subscription.SubscriptionChannelComponent();
        channel.setType(Subscription.SubscriptionChannelType.RESTHOOK);
        channel.setEndpoint(FhirConfig.getSubscriptionEndpoint()); // seu endpoint receptor
        channel.setPayload("application/fhir+json");
        channel.addHeader("X-FHIR-Subscription: true");
        channel.addHeader("X-Skip-Handshake: true");
        channel.addHeader("X-Subscription-No-Handshake: true");
        channel.addHeader("X-HAPI-Subscription: skip-handshake");
        channel.addHeader("X-Allow-Insecure: true");
        channel.addHeader("X-Bypass-TLS: true");

        subscription.setChannel(channel);
        Subscription createdSubscription = createSubscription(subscription);
        verifySubscription(createdSubscription.getIdElement().getIdPart());
        createdSubscriptionId = createdSubscription.getIdElement().getIdPart();
        return subscription;
    }

    private static Subscription createSubscription(Subscription subscription) {
        FhirContext ctx = FhirContext.forR4();
        ctx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);

        ctx.getRestfulClientFactory().setConnectTimeout(30 * 1000);
        ctx.getRestfulClientFactory().setSocketTimeout(30 * 1000);
        IGenericClient client = ctx.newRestfulGenericClient("https://hapi.fhir.org/baseR4");

        System.out.println(subscription.getId());
        MethodOutcome methodOutcome = client.create().resource(subscription).execute();

        if (methodOutcome.getOperationOutcome() != null) {
            System.out.println("OperationOutcome: " + methodOutcome.getOperationOutcome());
        }

        System.out.println("Subscription registrada com sucesso!");
        return (Subscription) methodOutcome.getResource();

    }

    private static void verifySubscription(String subscriptionId) {
        FhirContext ctx = FhirContext.forR4();
        IGenericClient client = ctx.newRestfulGenericClient("https://hapi.fhir.org/baseR4");

        try {
            Subscription subscription = client.read()
                    .resource(Subscription.class)
                    .withId(subscriptionId)
                    .execute();

            System.out.println("\n🔍 VERIFICAÇÃO DA SUBSCRIPTION:");
            System.out.println("ID: " + subscription.getIdElement().getIdPart());
            System.out.println("Status: " + subscription.getStatus());
            System.out.println("Criteria: " + subscription.getCriteria());
            System.out.println("Endpoint: " + subscription.getChannel().getEndpoint());
            System.out.println("Tipo: " + subscription.getChannel().getType());

            // Verificar se há erros
            if (subscription.getError() != null) {
                System.out.println("❌ Erro: " + subscription.getError());
            }

            // ✅ VERIFICAR: Se o endpoint é acessível
            System.out.println("✅ Subscription verificada com sucesso!");

        } catch (Exception e) {
            System.err.println("❌ Erro ao verificar subscription: " + e.getMessage());
        }
    }

    public void closeSubscription() {
            FhirContext ctx = FhirContext.forR4();
            IGenericClient client = ctx.newRestfulGenericClient("https://hapi.fhir.org/baseR4");

            Subscription subscription = client.read()
                    .resource(Subscription.class)
                    .withId(createdSubscriptionId)
                    .execute();

            // Mudar status para "off"
            subscription.setStatus(Subscription.SubscriptionStatus.OFF);

            // Atualizar no servidor
            client.update()
                    .resource(subscription)
                    .withId(createdSubscriptionId)
                    .execute();

    }

    private void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            closeSubscription();
        }));
    }
}
