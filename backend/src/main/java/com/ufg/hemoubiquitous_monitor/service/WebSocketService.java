package com.ufg.hemoubiquitous_monitor.service;

import com.ufg.hemoubiquitous_monitor.model.EpidemicNotification;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @SendTo("/topic/epidemic-cases")
    public void sendMessage(String username, String message) {
         new EpidemicNotification(10L, "Goiânia", "50%");

        messagingTemplate.convertAndSendToUser(username, "/queue/reply", message);
    }
}