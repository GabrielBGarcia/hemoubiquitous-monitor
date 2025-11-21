package com.ufg.hemoubiquitous_monitor.service;

import com.google.firebase.messaging.*;
import com.ufg.hemoubiquitous_monitor.model.EpidemicNotification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.ufg.hemoubiquitous_monitor.exception.FirebaseMessagingException;

import java.util.List;

@Service
public class NotificationService {
    private final SimpMessagingTemplate messagingTemplate;
    private final FirebaseMessaging firebaseMessaging;

    public NotificationService(SimpMessagingTemplate messagingTemplate, FirebaseMessaging firebaseMessaging) {
        this.messagingTemplate = messagingTemplate;
        this.firebaseMessaging = firebaseMessaging;
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
        } catch (FirebaseMessagingException | com.google.firebase.messaging.FirebaseMessagingException e) {
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
             firebaseMessaging.send(message);
        } catch (FirebaseMessagingException | com.google.firebase.messaging.FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    public void subscribeToTopic(String token, String topic) throws FirebaseMessagingException, com.google.firebase.messaging.FirebaseMessagingException {
        TopicManagementResponse response = firebaseMessaging
                .subscribeToTopic(List.of(token), topic);


        if (response.getFailureCount() > 0) {
            System.out.println(response.getErrors());
            throw new FirebaseMessagingException("Falha ao inscrever no tópico");
        }
    }


    public void unsubscribeFromTopic(String token, String topic) throws FirebaseMessagingException, com.google.firebase.messaging.FirebaseMessagingException {
        TopicManagementResponse response = FirebaseMessaging.getInstance()
                .unsubscribeFromTopic(List.of(token), topic);

        if (response.getFailureCount() > 0) {
            throw new FirebaseMessagingException("Falha ao desinscrever do tópico");
        }
        System.out.println("Token removido do tópico: " + topic);
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
        } catch (FirebaseMessagingException | com.google.firebase.messaging.FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }
}
