package com.ufg.hemoubiquitous_monitor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
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
        this.serviceAccount = serviceAccount;
    }
}
