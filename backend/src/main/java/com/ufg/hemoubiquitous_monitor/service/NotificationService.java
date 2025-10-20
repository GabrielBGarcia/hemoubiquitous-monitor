package com.ufg.hemoubiquitous_monitor.service;

import com.ufg.hemoubiquitous_monitor.model.EpidemicNotification;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

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
}
