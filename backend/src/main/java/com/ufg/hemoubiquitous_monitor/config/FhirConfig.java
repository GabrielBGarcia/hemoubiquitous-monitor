package com.ufg.hemoubiquitous_monitor.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FhirConfig {
    private static final String PROPERTIES_FILE = "fhir-config.properties";
    private static Properties properties;

    static {
        properties = new Properties();
        try (InputStream input = FhirConfig.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input != null) {
                properties.load(input);
            } else {
                throw new RuntimeException("Arquivo " + PROPERTIES_FILE + " não encontrado no classpath");
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar arquivo de configuração", e);
        }
    }

    public static String getFhirServerUrl() {
        return properties.getProperty("fhir.server.url", "https://hapi.fhir.org/baseR4");
    }

    public static int getConnectTimeout() {
        return Integer.parseInt(properties.getProperty("fhir.server.timeout.connect", "30000"));
    }

    public static int getSocketTimeout() {
        return Integer.parseInt(properties.getProperty("fhir.server.timeout.socket", "30000"));
    }

    public static String getServerValidationMode() {
        return properties.getProperty("fhir.server.validation.mode", "NEVER");
    }

    public static String getSubscriptionEndpoint() {
        return properties.getProperty("ngrok.dinamic.url") + "/fhir/receptor/hemograma";
    }

    public static String getSubscriptionPayload() {
        return properties.getProperty("subscription.payload", "application/fhir+json");
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}