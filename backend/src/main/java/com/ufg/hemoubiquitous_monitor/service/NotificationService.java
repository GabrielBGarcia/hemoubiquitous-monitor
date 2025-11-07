package com.ufg.hemoubiquitous_monitor.service;

import com.google.firebase.messaging.*;
import com.ufg.hemoubiquitous_monitor.model.EpidemicNotification;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyEpidemicSuspect(String uf, String region) {
        System.out.println("Notificando suspeita de epidemia para " + uf + " - " + region);
        messagingTemplate.convertAndSend("/topic/notify/" + uf + "/" + region, "Há suspeita de epidemia na região " + region);
    }

    public void sendToDevice(String deviceToken, String title, String body) {
        Message message = Message.builder()
                .setToken(deviceToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Notificação enviada: " + response);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendToTopic(String topic, String title, String body) {
        Message message = Message.builder()
                .setTopic(topic)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendToMultipleDevices(List<String> tokens, String title, String body) {
        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance()
                    .sendMulticast(message);
            System.out.println(response.getSuccessCount() + " mensagens enviadas");
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }
}
