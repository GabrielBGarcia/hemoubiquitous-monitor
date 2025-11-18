package com.ufg.hemoubiquitous_monitor.service;

import com.google.firebase.messaging.*;
import java.util.List;
import java.util.Map;

public class FCMService {

    public String sendNotificationToToken(String token, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setToken(token)
                    .putData(title, body)
                    .build();

            return FirebaseMessaging.getInstance().send(message);

        } catch (FirebaseMessagingException e) {
            System.err.println("Erro ao enviar notificação: " + e.getMessage());
            return null;
        }
    }

    public String sendNotificationWithData(String token, String title, String body,
                                           Map<String, String> data) {
        try {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            // Adiciona dados customizados
            if (data != null) {
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    messageBuilder.putData(entry.getKey(), entry.getValue());
                }
            }

            return FirebaseMessaging.getInstance().send(messageBuilder.build());

        } catch (FirebaseMessagingException e) {
            System.err.println("Erro ao enviar notificação: " + e.getMessage());
            return null;
        }
    }

    public BatchResponse sendNotificationToMultipleTokens(List<String> tokens,
                                                          String title, String body) {
        try {
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            return FirebaseMessaging.getInstance().sendMulticast(message);

        } catch (FirebaseMessagingException e) {
            System.err.println("Erro ao enviar notificações múltiplas: " + e.getMessage());
            return null;
        }
    }
}