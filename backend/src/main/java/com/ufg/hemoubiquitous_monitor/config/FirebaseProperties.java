package com.ufg.hemoubiquitous_monitor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "firebase")
public class FirebaseProperties {

    private Resource serviceAccount;

    public Resource getServiceAccount() {
        return serviceAccount;
    }

    public void setServiceAccount(Resource serviceAccount) {
        this.serviceAccount = new ClassPathResource(
                "hemoubiquitous-fhir-firebase-adminsdk-fbsvc-42a25b9c4b.json"
        );
    }
}
