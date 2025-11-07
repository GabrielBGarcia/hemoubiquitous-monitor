package com.ufg.hemoubiquitous_monitor.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.FileInputStream;
import java.io.IOException;

public class FirebaseConfig {
    public static void initialize() throws IOException {
        FileInputStream serviceAccount =
                new FileInputStream("/com/ufg/hemoubiquitous_monitor/config/hemoubiquitous-fhir-firebase-adminsdk-fbsvc-740de4fdaf.json");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);
    }
}