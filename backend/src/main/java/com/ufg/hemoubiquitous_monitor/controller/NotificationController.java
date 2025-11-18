package com.ufg.hemoubiquitous_monitor.controller;

import com.ufg.hemoubiquitous_monitor.model.dto.RegisterInTopicDto;
import com.ufg.hemoubiquitous_monitor.model.dto.TokenRequestDto;
import com.ufg.hemoubiquitous_monitor.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/register")
    public ResponseEntity<?> registerToken(@RequestBody RegisterInTopicDto registerDto) {
        try {
            // Inscrever token no tópico
            notificationService.subscribeToTopic(
                    registerDto.token,
                    registerDto.topic
            );

            return ResponseEntity.ok(Map.of("message", "Token registrado com sucesso"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/unregister")
    public ResponseEntity<?> unregisterToken(@RequestBody RegisterInTopicDto registerDto) {
        try {
            notificationService.unsubscribeFromTopic(
                    registerDto.token,
                    registerDto.topic
            );
            return ResponseEntity.ok(Map.of("message", "Token removido"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}